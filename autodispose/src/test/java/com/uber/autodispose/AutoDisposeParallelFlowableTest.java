/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.autodispose;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.AutoDispose.autoDisposable;

import com.uber.autodispose.test.RxErrorsRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Subscriber;

public class AutoDisposeParallelFlowableTest extends PluginsMatrixTest {

  private static final int DEFAULT_PARALLELISM = 2;

  @Rule public final RxErrorsRule rule = new RxErrorsRule();

  public AutoDisposeParallelFlowableTest(boolean hideProxies) {
    super(hideProxies);
  }

  @Test
  public void ifParallelism_and_subscribersCount_dontMatch_shouldFail() {
    TestSubscriber<Integer> subscriber = new TestSubscriber<>();
    CompletableSubject scope = CompletableSubject.create();

    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {subscriber};
    Flowable.just(1, 2)
        .parallel(DEFAULT_PARALLELISM)
        .as(autoDisposable(scope))
        .subscribe(subscribers);

    List<Throwable> errors = subscriber.errors();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void autoDispose_withMaybe_normal() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    CompletableSubject scope = CompletableSubject.create();

    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};
    source.parallel(DEFAULT_PARALLELISM).as(autoDisposable(scope)).subscribe(subscribers);
    firstSubscriber.assertSubscribed();
    secondSubscriber.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    source.onNext(2);
    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);

    source.onNext(3);
    source.onNext(4);
    source.onComplete();
    firstSubscriber.assertValues(1, 3);
    firstSubscriber.assertComplete();
    secondSubscriber.assertValues(2, 4);
    secondSubscriber.assertComplete();
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withMaybe_interrupted() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    CompletableSubject scope = CompletableSubject.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source.parallel(DEFAULT_PARALLELISM).as(autoDisposable(scope)).subscribe(subscribers);

    firstSubscriber.assertSubscribed();
    secondSubscriber.assertSubscribed();

    source.onNext(1);
    source.onNext(2);
    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);

    scope.onComplete();
    source.onNext(3);

    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = TestUtil.makeProvider(scope);
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source.parallel(DEFAULT_PARALLELISM).as(autoDisposable(provider)).subscribe(subscribers);
    firstSubscriber.assertSubscribed();
    secondSubscriber.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    source.onNext(2);
    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);

    source.onNext(3);
    source.onNext(4);

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    firstSubscriber.assertValues(1, 3);
    secondSubscriber.assertValues(2, 4);

    scope.onComplete();
    source.onNext(5);
    source.onNext(6);

    firstSubscriber.assertValues(1, 3);
    secondSubscriber.assertValues(2, 4);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withScopeProviderCompleted_shouldNotReportDoubleSubscriptions() {
    TestSubscriber<Object> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Object> secondSubscriber = new TestSubscriber<>();
    //noinspection unchecked
    Subscriber<Object>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};
    PublishProcessor.create()
        .parallel(DEFAULT_PARALLELISM)
        .as(autoDisposable(ScopeProvider.UNBOUND))
        .subscribe(subscribers);
    firstSubscriber.assertNoValues();
    firstSubscriber.assertNoErrors();
    secondSubscriber.assertNoValues();
    secondSubscriber.assertNoErrors();
    rule.assertNoErrors();
  }

  @Test
  public void unbound_shouldStillPassValues() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source
        .parallel(DEFAULT_PARALLELISM)
        .as(autoDisposable(ScopeProvider.UNBOUND))
        .subscribe(subscribers);

    source.onNext(1);
    source.onNext(2);
    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);
    firstSubscriber.dispose();
    secondSubscriber.dispose();
  }

  @Test
  public void hideProxies() {
    ParallelFlowableSubscribeProxy proxy =
        Flowable.never().parallel().as(autoDisposable(ScopeProvider.UNBOUND));
    // If hideProxies is disabled, the underlying return should be the direct AutoDispose type.
    if (hideProxies) {
      assertThat(proxy).isNotInstanceOf(ParallelFlowable.class);
    } else {
      assertThat(proxy).isInstanceOf(AutoDisposeParallelFlowable.class);
    }
  }
}
