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
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subjects.SingleSubject;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.TestUtil.makeLifecycleProvider;
import static com.uber.autodispose.TestUtil.makeProvider;

public class AutoDisposeSingleObserverTest {

  @After public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test public void autoDispose_withMaybe_normal() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    SingleSubject<Integer> source = SingleSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.to(new SingleScoper<Integer>(lifecycle))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    // Got the event
    source.onSuccess(1);
    assertThat(o.takeSuccess()).isEqualTo(1);

    // Nothing more, lifecycle disposed too
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withSuperClassGenerics_compilesFine() {
    Single.just(new BClass())
        .to(new SingleScoper<AClass>(Maybe.never()))
        .subscribe(new Consumer<AClass>() {
          @Override public void accept(@NonNull AClass aClass) throws Exception {

          }
        });
  }

  @Test public void autoDispose_noGenericsOnEmpty_isFine() {
    Single.just(new BClass())
        .to(new SingleScoper<>(Maybe.never()))
        .subscribe();
  }

  @Test public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    SingleSubject<Integer> source = SingleSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.to(new SingleScoper<Integer>(lifecycle))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    // Lifecycle ends
    lifecycle.onSuccess(2);
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();

    // Event if upstream emits, no one is listening
    source.onSuccess(2);
    o.assertNoMoreEvents();
  }

  @Test public void autoDispose_withProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    SingleSubject<Integer> source = SingleSubject.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.to(new SingleScoper<Integer>(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onSuccess(3);
    o.takeSuccess();

    // All cleaned up
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    SingleSubject<Integer> source = SingleSubject.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.to(new SingleScoper<Integer>(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    // Lifecycle ends
    scope.onSuccess(3);
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();

    // No one is listening even if upstream finally does emit
    source.onSuccess(3);
    o.assertNoMoreEvents();
  }

  @Test public void autoDispose_withLifecycleProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    SingleSubject<Integer> source = SingleSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    source.to(new SingleScoper<Integer>(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onNext(1);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onSuccess(3);
    o.takeSuccess();

    // All cleaned up
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withLifecycleProvider_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    SingleSubject<Integer> source = SingleSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    source.to(new SingleScoper<Integer>(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onNext(1);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    // Lifecycle ends
    lifecycle.onNext(3);
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();

    // No one is listening even if upstream finally does emit
    source.onSuccess(3);
    o.assertNoMoreEvents();
  }

  @Test public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    RecordingObserver<Integer> o = new RecordingObserver<>();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    Single.just(1)
        .to(new SingleScoper<Integer>(provider))
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
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    Single.just(1)
        .to(new SingleScoper<Integer>(provider))
        .subscribe(o);

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleEndedException.class);
  }

  @Test public void autoDispose_withProviderAndNoOpPlugin_withoutStarting_shouldFailSilently() {
    AutoDisposePlugins.setOutsideLifecycleHandler(new Consumer<OutsideLifecycleException>() {
      @Override
      public void accept(OutsideLifecycleException e) throws Exception { }
    });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestObserver<Integer> o = new TestObserver<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    SingleSubject<Integer> source = SingleSubject.create();
    source
            .to(new SingleScoper<Integer>(provider))
            .subscribeWith(o);

    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test public void autoDispose_withProviderAndNoOpPlugin_afterEnding_shouldFailSilently() {
    AutoDisposePlugins.setOutsideLifecycleHandler(new Consumer<OutsideLifecycleException>() {
      @Override
      public void accept(OutsideLifecycleException e) throws Exception {
        // Noop
      }
    });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestObserver<Integer> o = new TestObserver<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    SingleSubject<Integer> source = SingleSubject.create();
    source
            .to(new SingleScoper<Integer>(provider))
            .subscribe(o);

    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test public void autoDispose_withProviderAndPlugin_withoutStarting_shouldFailWithExp() {
    AutoDisposePlugins.setOutsideLifecycleHandler(new Consumer<OutsideLifecycleException>() {
      @Override
      public void accept(OutsideLifecycleException e) throws Exception {
        // Wrap in an IllegalStateException so we can verify this is the exception we see on the
        // other side
        throw new IllegalStateException(e);
      }
    });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestObserver<Integer> o = new TestObserver<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    SingleSubject<Integer> source = SingleSubject.create();
    source
            .to(new SingleScoper<Integer>(provider))
            .subscribe(o);

    o.assertNoValues();
    o.assertError(new Predicate<Throwable>() {
      @Override
      public boolean test(Throwable throwable) throws Exception {
        return throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideLifecycleException;
      }
    });
  }

  @Test public void verifyCancellation() throws Exception {
    final AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    Single<Integer> source = Single.create(new SingleOnSubscribe<Integer>() {
      @Override public void subscribe(SingleEmitter<Integer> e) throws Exception {
        e.setCancellable(new Cancellable() {
          @Override public void cancel() throws Exception {
            i.incrementAndGet();
          }
        });
      }
    });
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.to(new SingleScoper<Integer>(lifecycle))
        .subscribe();

    assertThat(i.get()).isEqualTo(0);
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onSuccess(0);

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(lifecycle.hasObservers()).isFalse();
  }
}
