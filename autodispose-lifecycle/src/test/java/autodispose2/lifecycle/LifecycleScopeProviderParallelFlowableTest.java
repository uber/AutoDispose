/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.lifecycle;

import static autodispose2.AutoDispose.autoDisposable;
import static autodispose2.lifecycle.TestUtil.makeLifecycleProvider;
import static com.google.common.truth.Truth.assertThat;

import autodispose2.AutoDisposePlugins;
import autodispose2.OutsideScopeException;
import autodispose2.test.RxErrorsRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Subscriber;

public class LifecycleScopeProviderParallelFlowableTest {

  private static final int DEFAULT_PARALLELISM = 2;

  @Rule public final RxErrorsRule rule = new RxErrorsRule();

  @Before
  @After
  public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test
  public void autoDispose_withLifecycleProvider() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source.parallel(DEFAULT_PARALLELISM).to(autoDisposable(provider)).subscribe(subscribers);
    assertThat(firstSubscriber.hasSubscription()).isTrue();
    assertThat(secondSubscriber.hasSubscription()).isTrue();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    source.onNext(2);
    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);

    source.onNext(3);
    source.onNext(4);

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    firstSubscriber.assertValues(1, 3);
    secondSubscriber.assertValues(2, 4);

    lifecycle.onNext(3);
    source.onNext(5);
    source.onNext(6);

    firstSubscriber.assertValues(1, 3);
    secondSubscriber.assertValues(2, 4);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    Flowable.just(1, 2)
        .parallel(DEFAULT_PARALLELISM)
        .to(autoDisposable(provider))
        .subscribe(subscribers);

    firstSubscriber.assertError(LifecycleNotStartedException.class);
    secondSubscriber.assertError(LifecycleNotStartedException.class);
  }

  @Test
  public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    Flowable.just(1, 2)
        .parallel(DEFAULT_PARALLELISM)
        .to(autoDisposable(provider))
        .subscribe(subscribers);

    firstSubscriber.assertError(LifecycleEndedException.class);
    secondSubscriber.assertError(LifecycleEndedException.class);
  }

  @Test
  public void autoDispose_withProviderAndNoOpPlugin_withoutStarting_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> {});
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source.parallel(DEFAULT_PARALLELISM).to(autoDisposable(provider)).subscribe(subscribers);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();

    firstSubscriber.assertNoValues();
    firstSubscriber.assertNoErrors();
    secondSubscriber.assertNoValues();
    secondSubscriber.assertNoErrors();
  }

  @Test
  public void autoDispose_withProviderAndNoOpPlugin_afterEnding_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(
        e -> {
          // Noop
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source.parallel(DEFAULT_PARALLELISM).to(autoDisposable(provider)).subscribe(subscribers);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    firstSubscriber.assertNoValues();
    firstSubscriber.assertNoErrors();
  }

  @Test
  public void autoDispose_withProviderAndPlugin_withoutStarting_shouldFailWithExp() {
    AutoDisposePlugins.setOutsideScopeHandler(
        e -> {
          throw new IllegalStateException(e);
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source.parallel(DEFAULT_PARALLELISM).to(autoDisposable(provider)).subscribe(subscribers);

    firstSubscriber.assertNoValues();
    firstSubscriber.assertError(
        throwable ->
            throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideScopeException);
    secondSubscriber.assertNoValues();
    secondSubscriber.assertError(
        throwable ->
            throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideScopeException);
  }
}
