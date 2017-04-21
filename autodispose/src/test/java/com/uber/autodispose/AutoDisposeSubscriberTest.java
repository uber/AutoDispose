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

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subscribers.TestSubscriber;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class AutoDisposeSubscriberTest {

  @After public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test public void autoDispose_withMaybe_normal() {
    TestSubscriber<Integer> o = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    Disposable d = source.to(new FlowableScoper<Integer>(lifecycle))
        .subscribeWith(o);
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);
    source.onComplete();
    o.assertValues(1, 2);
    o.assertComplete();
    assertThat(d.isDisposed()).isFalse();   // Because it completes normally
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withSuperClassGenerics_compilesFine() {
    Flowable.just(new BClass())
        .to(new FlowableScoper<AClass>(Maybe.never()))
        .subscribe(new Consumer<AClass>() {
          @Override public void accept(@NonNull AClass aClass) throws Exception {

          }
        });
  }

  @Test public void autoDispose_noGenericsOnEmpty_isFine() {
    Flowable.just(new BClass())
        .to(new FlowableScoper<>(Maybe.never()))
        .subscribe();
  }

  @Test public void autoDispose_withMaybe_interrupted() {
    TestSubscriber<Integer> o = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.to(new FlowableScoper<Integer>(lifecycle))
        .subscribe(o);
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    lifecycle.onSuccess(2);
    source.onNext(2);

    // No more events
    o.assertValue(1);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider() {
    TestSubscriber<Integer> o = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    ScopeProvider provider = TestUtil.makeProvider(scope);
    source.to(new FlowableScoper<Integer>(provider))
        .subscribe(o);
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();
    o.assertValues(1, 2);

    scope.onSuccess(3);
    source.onNext(3);

    // Nothing new
    o.assertValues(1, 2);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withLifecycleProvider() {
    TestSubscriber<Integer> o = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    source.to(new FlowableScoper<Integer>(provider))
        .subscribe(o);
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    lifecycle.onNext(1);
    source.onNext(2);

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();
    o.assertValues(1, 2);

    lifecycle.onNext(3);
    source.onNext(3);

    // Nothing new
    o.assertValues(1, 2);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> o = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    Flowable.just(1)
        .to(new FlowableScoper<Integer>(provider))
        .subscribe(o);

    List<Throwable> errors = o.errors();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test public void autoDispose_withProviderAndPlugin_withoutStarting_shouldFailPlugin() {
    final RecordingObserver<OutsideLifecycleException> errorHandler = new RecordingObserver<>();
    AutoDisposePlugins.setOutsideLifecycleHandler(new Consumer<OutsideLifecycleException>() {
      @Override
      public void accept(OutsideLifecycleException e) throws Exception {
        errorHandler.onNext(e);
      }
    });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> o = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    Flowable.just(1)
            .to(new FlowableScoper<Integer>(provider))
            .subscribe(o);

    o.assertNotSubscribed();
    assertThat(errorHandler.takeNext()).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestSubscriber<Integer> o = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    Flowable.just(1)
        .to(new FlowableScoper<Integer>(provider))
        .subscribe(o);

    List<Throwable> errors = o.errors();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).isInstanceOf(LifecycleEndedException.class);
  }

  @Test public void autoDispose_withProviderAndPlugin_afterLifecycle_shouldFailPlugin() {
    final RecordingObserver<OutsideLifecycleException> errorHandler = new RecordingObserver<>();
    AutoDisposePlugins.setOutsideLifecycleHandler(new Consumer<OutsideLifecycleException>() {
      @Override
      public void accept(OutsideLifecycleException e) throws Exception {
        errorHandler.onNext(e);
      }
    });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestSubscriber<Integer> o = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    Flowable.just(1)
            .to(new FlowableScoper<Integer>(provider))
            .subscribe(o);

    o.assertNotSubscribed();
    assertThat(errorHandler.takeNext()).isInstanceOf(LifecycleEndedException.class);
  }

  @Test public void verifyCancellation() throws Exception {
    final AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    final FlowableEmitter<Integer>[] emitter = new FlowableEmitter[1];
    Flowable<Integer> source = Flowable.create(new FlowableOnSubscribe<Integer>() {
      @Override public void subscribe(FlowableEmitter<Integer> e) throws Exception {
        e.setCancellable(new Cancellable() {
          @Override public void cancel() throws Exception {
            i.incrementAndGet();
          }
        });
        emitter[0] = e;
      }
    }, BackpressureStrategy.LATEST);
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.to(new FlowableScoper<Integer>(lifecycle))
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
