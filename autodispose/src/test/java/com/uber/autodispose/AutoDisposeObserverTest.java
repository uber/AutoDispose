/*
 * Copyright (C) 2017. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uber.autodispose;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subjects.PublishSubject;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class AutoDisposeObserverTest {

  @Test public void autoDispose_withMaybe_normal() {
    TestObserver<Integer> o = new TestObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    Disposable d = source.to(new ObservableScoper<Integer>(lifecycle))
        .subscribeWith(o);
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);
    source.onComplete();
    o.assertValues(1, 2);
    o.assertComplete();
    assertThat(d.isDisposed()).isFalse();   // Because it completed normally, was not disposed.
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withSuperClassGenerics_compilesFine() {
    Observable.just(new BClass())
        .to(new ObservableScoper<AClass>(Maybe.never()))
        .subscribe(new Consumer<AClass>() {
          @Override public void accept(@NonNull AClass aClass) throws Exception {

          }
        });
  }

  @Test public void autoDispose_noGenericsOnEmpty_isFine() {
    Observable.just(new BClass())
        .to(new ObservableScoper<>(Maybe.never()))
        .subscribe();
  }

  @Test public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.to(new ObservableScoper<Integer>(lifecycle))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.onSuccess(2);
    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    ScopeProvider provider = TestUtil.makeProvider(scope);
    source.to(new ObservableScoper<Integer>(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    source.onNext(2);

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();
    assertThat(o.takeNext()).isEqualTo(2);

    scope.onSuccess(3);
    source.onNext(3);

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withLifecycleProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    source.to(new ObservableScoper<Integer>(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.onNext(1);
    source.onNext(2);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();
    assertThat(o.takeNext()).isEqualTo(2);

    lifecycle.onNext(3);
    source.onNext(3);

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    RecordingObserver<Integer> o = new RecordingObserver<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    Observable.just(1)
        .to(new ObservableScoper<Integer>(provider))
        .subscribe(o);

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    RecordingObserver<Integer> o = new RecordingObserver<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    Observable.just(1)
        .to(new ObservableScoper<Integer>(provider))
        .subscribe(o);

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleEndedException.class);
  }

  @Test public void verifyCancellation() throws Exception {
    final AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    final ObservableEmitter<Integer>[] emitter = new ObservableEmitter[1];
    Observable<Integer> source = Observable.create(new ObservableOnSubscribe<Integer>() {
      @Override public void subscribe(ObservableEmitter<Integer> e) throws Exception {
        e.setCancellable(new Cancellable() {
          @Override public void cancel() throws Exception {
            i.incrementAndGet();
          }
        });
        emitter[0] = e;
      }
    });
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.to(new ObservableScoper<Integer>(lifecycle))
        .subscribe();

    assertThat(i.get()).isEqualTo(0);
    assertThat(lifecycle.hasObservers()).isTrue();

    emitter[0].onNext(1);

    lifecycle.onSuccess(0);
    emitter[0].onNext(2);

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(lifecycle.hasObservers()).isFalse();
  }
}
