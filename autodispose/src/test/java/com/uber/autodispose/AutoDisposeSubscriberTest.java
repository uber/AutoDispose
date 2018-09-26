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
import com.uber.autodispose.test.RxErrorsRule;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subscribers.TestSubscriber;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Subscriber;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.TestUtil.outsideScopeProvider;

public class AutoDisposeSubscriberTest {

  @Rule public RxErrorsRule rule = new RxErrorsRule();

  @Test public void autoDispose_withMaybe_normal() {
    TestSubscriber<Integer> o = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    CompletableSubject scope = CompletableSubject.create();
    Disposable d = source.as(AutoDispose.<Integer>autoDisposable(scope))
        .subscribeWith(o);
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);
    source.onComplete();
    o.assertValues(1, 2);
    o.assertComplete();
    assertThat(d.isDisposed()).isFalse();   // Because it completes normally
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withSuperClassGenerics_compilesFine() {
    Flowable.just(new BClass())
        .as(AutoDispose.<BClass>autoDisposable(ScopeProvider.UNBOUND))
        .subscribe((Consumer<AClass>) aClass -> {

        });
  }

  @Test public void autoDispose_withMaybe_interrupted() {
    PublishProcessor<Integer> source = PublishProcessor.create();
    CompletableSubject scope = CompletableSubject.create();
    TestSubscriber<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(scope))
        .test();
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onComplete();
    source.onNext(2);

    // No more events
    o.assertValue(1);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider() {
    PublishProcessor<Integer> source = PublishProcessor.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = TestUtil.makeProvider(scope);
    TestSubscriber<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(provider))
        .test();
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();
    o.assertValues(1, 2);

    scope.onComplete();
    source.onNext(3);

    // Nothing new
    o.assertValues(1, 2);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void verifySubscriberDelegate() {
    final AtomicReference<Subscriber> atomicSubscriber = new AtomicReference<>();
    final AtomicReference<Subscriber> atomicAutoDisposingSubscriber = new AtomicReference<>();
    try {
      RxJavaPlugins.setOnFlowableSubscribe((source, subscriber) -> {
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
      });
      Flowable.just(1)
          .as(AutoDispose.<Integer>autoDisposable(ScopeProvider.UNBOUND))
          .subscribe();

      assertThat(atomicAutoDisposingSubscriber.get()).isNotNull();
      assertThat(atomicAutoDisposingSubscriber.get()).isInstanceOf(AutoDisposingSubscriber.class);
      assertThat(((AutoDisposingSubscriber) atomicAutoDisposingSubscriber.get()).delegateSubscriber()).isNotNull();
      assertThat(((AutoDisposingSubscriber) atomicAutoDisposingSubscriber.get()).delegateSubscriber()).isSameAs(
          atomicSubscriber.get());
    } finally {
      RxJavaPlugins.reset();
    }
  }

  @Test public void verifyCancellation() {
    final AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    final FlowableEmitter<Integer>[] emitter = new FlowableEmitter[1];
    Flowable<Integer> source = Flowable.create(e -> {
      e.setCancellable(i::incrementAndGet);
      emitter[0] = e;
    }, BackpressureStrategy.LATEST);
    CompletableSubject scope = CompletableSubject.create();
    source.as(AutoDispose.<Integer>autoDisposable(scope))
        .subscribe();

    assertThat(i.get()).isEqualTo(0);
    assertThat(scope.hasObservers()).isTrue();

    emitter[0].onNext(1);

    scope.onComplete();
    emitter[0].onNext(2);

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withScopeProviderCompleted_shouldNotReportDoubleSubscriptions() {
    TestSubscriber<Object> o = PublishProcessor.create()
        .as(AutoDispose.autoDisposable(ScopeProvider.UNBOUND))
        .test();
    o.assertNoValues();
    o.assertNoErrors();

    rule.assertNoErrors();
  }

  @Test public void unbound_shouldStillPassValues() {
    PublishProcessor<Integer> s = PublishProcessor.create();
    TestSubscriber<Integer> o = s.as(AutoDispose.<Integer>autoDisposable(ScopeProvider.UNBOUND))
        .test();

    s.onNext(1);
    o.assertValue(1);
    o.dispose();
  }

  @Test public void autoDispose_outsideScope_withProviderAndNoOpPlugin_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> { });
    ScopeProvider provider = outsideScopeProvider();
    PublishProcessor<Integer> source = PublishProcessor.create();
    TestSubscriber<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(provider))
        .test();

    assertThat(source.hasSubscribers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test public void autoDispose_outsideScope_withProviderAndPlugin_shouldFailWithWrappedExp() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> {
      // Wrap in an IllegalStateException so we can verify this is the exception we see on the
      // other side
      throw new IllegalStateException(e);
    });
    ScopeProvider provider = outsideScopeProvider();
    TestSubscriber<Integer> o = PublishProcessor.<Integer>create().as(AutoDispose.<Integer>autoDisposable(provider))
        .test();

    o.assertNoValues();
    o.assertError(throwable -> throwable instanceof IllegalStateException && throwable.getCause() instanceof OutsideScopeException);
  }
}
