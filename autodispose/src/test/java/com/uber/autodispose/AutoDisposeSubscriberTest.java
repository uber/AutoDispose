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

import com.uber.autodispose.observers.AutoDisposingSubscriber;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subscribers.TestSubscriber;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Test;
import org.reactivestreams.Subscriber;

import static com.google.common.truth.Truth.assertThat;

public class AutoDisposeSubscriberTest {

  @After public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test public void autoDispose_withMaybe_normal() {
    TestSubscriber<Integer> o = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    Disposable d = source.to(AutoDispose.with(lifecycle).<Integer>flowable())
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
        .to(AutoDispose.with(Maybe.never()).<AClass>flowable())
        .subscribe(new Consumer<AClass>() {
          @Override public void accept(AClass aClass) throws Exception {

          }
        });
  }

  @Test public void autoDispose_noGenericsOnEmpty_isFine() {
    Flowable.just(new BClass())
        .to(AutoDispose.with(Maybe.never())
            .flowable())
        .subscribe();
  }

  @Test public void autoDispose_withMaybe_interrupted() {
    TestSubscriber<Integer> o = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.to(AutoDispose.with(lifecycle).<Integer>flowable())
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
    source.to(AutoDispose.with(provider).<Integer>flowable())
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
    source.to(AutoDispose.with(provider).<Integer>flowable())
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
        .to(AutoDispose.with(provider).<Integer>flowable())
        .subscribe(o);

    List<Throwable> errors = o.errors();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestSubscriber<Integer> o = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    Flowable.just(1)
        .to(AutoDispose.with(provider).<Integer>flowable())
        .subscribe(o);

    List<Throwable> errors = o.errors();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).isInstanceOf(LifecycleEndedException.class);
  }

  @Test public void autoDispose_withProviderAndNoOpPlugin_withoutStarting_shouldFailSilently() {
    AutoDisposePlugins.setOutsideLifecycleHandler(new Consumer<OutsideLifecycleException>() {
      @Override public void accept(OutsideLifecycleException e) throws Exception { }
    });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> o = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    source.to(AutoDispose.with(provider).<Integer>flowable())
        .subscribe(o);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test public void autoDispose_withProviderAndNoOpPlugin_afterEnding_shouldFailSilently() {
    AutoDisposePlugins.setOutsideLifecycleHandler(new Consumer<OutsideLifecycleException>() {
      @Override public void accept(OutsideLifecycleException e) throws Exception {
        // Noop
      }
    });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestSubscriber<Integer> o = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    source.to(AutoDispose.with(provider).<Integer>flowable())
        .subscribe(o);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test public void autoDispose_withProviderAndPlugin_withoutStarting_shouldFailWithExp() {
    AutoDisposePlugins.setOutsideLifecycleHandler(new Consumer<OutsideLifecycleException>() {
      @Override public void accept(OutsideLifecycleException e) throws Exception {
        // Wrap in an IllegalStateException so we can verify this is the exception we see on the
        // other side
        throw new IllegalStateException(e);
      }
    });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> o = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    source.to(AutoDispose.with(provider).<Integer>flowable())
        .subscribe(o);

    o.assertNoValues();
    o.assertError(new Predicate<Throwable>() {
      @Override public boolean test(Throwable throwable) throws Exception {
        return throwable instanceof IllegalStateException
            && throwable.getCause() instanceof OutsideLifecycleException;
      }
    });
  }

  @Test public void verifySubscriberDelegate() {
    final AtomicReference<Subscriber> atomicSubscriber = new AtomicReference<>();
    final AtomicReference<Subscriber> atomicAutoDisposingSubscriber = new AtomicReference<>();
    try {
      RxJavaPlugins.setOnFlowableSubscribe(new BiFunction<Flowable, Subscriber, Subscriber>() {
        @Override public Subscriber apply(Flowable source, Subscriber subscriber) {
          if (atomicSubscriber.get() == null) {
            System.out.println(subscriber.getClass()
                .toString());
            atomicSubscriber.set(subscriber);
          } else if (atomicAutoDisposingSubscriber.get() == null) {
            System.out.println(subscriber.getClass()
                .toString());
            atomicAutoDisposingSubscriber.set(subscriber);
            RxJavaPlugins.setOnFlowableSubscribe(null);
          }
          return subscriber;
        }
      });
      Flowable.just(1)
          .to(AutoDispose.with(Maybe.never()).<Integer>flowable())
          .subscribe();

      assertThat(atomicAutoDisposingSubscriber.get()).isNotNull();
      assertThat(atomicAutoDisposingSubscriber.get()).isInstanceOf(AutoDisposingSubscriber.class);
      assertThat(((AutoDisposingSubscriber) atomicAutoDisposingSubscriber.get())
              .delegateSubscriber())
          .isNotNull();
      assertThat(((AutoDisposingSubscriber) atomicAutoDisposingSubscriber.get())
              .delegateSubscriber())
          .isSameAs(atomicSubscriber.get());
    } finally {
      RxJavaPlugins.reset();
    }
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
    source.to(AutoDispose.with(lifecycle).<Integer>flowable())
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
