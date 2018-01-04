package com.uber.autodispose;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Subscriber;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subscribers.TestSubscriber;

import static com.google.common.truth.Truth.assertThat;

public class AutoDisposeParallelFlowableTest {

  private static final int DEFAULT_PARALLELISM = 2;

  @Rule public final RxErrorsRule rule = new RxErrorsRule();

  @After public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test public void autoDispose_withMaybe_normal() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();

    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};
    source
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(lifecycle))
        .subscribe(subscribers);
    firstSubscriber.assertSubscribed();
    secondSubscriber.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

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
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withMaybe_interrupted() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(lifecycle))
        .subscribe(subscribers);

    firstSubscriber.assertSubscribed();
    secondSubscriber.assertSubscribed();

    source.onNext(1);
    source.onNext(2);
    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);

    lifecycle.onSuccess(2);
    source.onNext(3);

    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    ScopeProvider provider = TestUtil.makeProvider(scope);
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(subscribers);
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

    scope.onSuccess(3);
    source.onNext(5);
    source.onNext(6);

    firstSubscriber.assertValues(1, 3);
    secondSubscriber.assertValues(2, 4);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withLifecycleProvider() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(subscribers);
    firstSubscriber.assertSubscribed();
    secondSubscriber.assertSubscribed();

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

  @Test public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    Flowable.just(1, 2)
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(subscribers);

    List<Throwable> errors1 = firstSubscriber.errors();
    assertThat(errors1).hasSize(1);
    assertThat(errors1.get(0)).isInstanceOf(LifecycleNotStartedException.class);

    List<Throwable> errors2 = secondSubscriber.errors();
    assertThat(errors2).hasSize(1);
    assertThat(errors2.get(0)).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    Flowable.just(1, 2)
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(subscribers);

    List<Throwable> errors1 = firstSubscriber.errors();
    assertThat(errors1).hasSize(1);
    assertThat(errors1.get(0)).isInstanceOf(LifecycleEndedException.class);

    List<Throwable> errors2 = secondSubscriber.errors();
    assertThat(errors2).hasSize(1);
    assertThat(errors2.get(0)).isInstanceOf(LifecycleEndedException.class);
  }

  @Test public void autoDispose_withProviderAndNoOpPlugin_withoutStarting_shouldFailSilently() {
    AutoDisposePlugins.setOutsideLifecycleHandler(
        new Consumer<OutsideLifecycleException>() {
          @Override
          public void accept(OutsideLifecycleException e) throws Exception {
          }
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(subscribers);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();

    firstSubscriber.assertNoValues();
    firstSubscriber.assertNoErrors();
    secondSubscriber.assertNoValues();
    secondSubscriber.assertNoErrors();
  }

  @Test public void autoDispose_withProviderAndNoOpPlugin_afterEnding_shouldFailSilently() {
    AutoDisposePlugins.setOutsideLifecycleHandler(
        new Consumer<OutsideLifecycleException>() {
          @Override
          public void accept(OutsideLifecycleException e) {
            // Noop
          }
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(subscribers);

    assertThat(source.hasSubscribers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    firstSubscriber.assertNoValues();
    firstSubscriber.assertNoErrors();
  }

  @Test public void autoDispose_withProviderAndPlugin_withoutStarting_shouldFailWithExp() {
    AutoDisposePlugins.setOutsideLifecycleHandler(
        new Consumer<OutsideLifecycleException>() {
          @Override
          public void accept(OutsideLifecycleException e) {
            throw new IllegalStateException(e);
          }
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    LifecycleScopeProvider<Integer> provider = TestUtil.makeLifecycleProvider(lifecycle);
    PublishProcessor<Integer> source = PublishProcessor.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(subscribers);

    firstSubscriber.assertNoValues();
    firstSubscriber.assertError(
        new Predicate<Throwable>() {
          @Override
          public boolean test(Throwable throwable) {
            return throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideLifecycleException;
          }
        });
    secondSubscriber.assertNoValues();
    secondSubscriber.assertError(
        new Predicate<Throwable>() {
          @Override
          public boolean test(Throwable throwable) throws Exception {
            return throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideLifecycleException;
          }
        });
  }

  @Test public void autoDispose_withScopeProviderCompleted_shouldNotReportDoubleSubscriptions() {
    TestSubscriber<Object> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Object> secondSubscriber = new TestSubscriber<>();
    //noinspection unchecked
    Subscriber<Object>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};
    PublishProcessor.create()
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.autoDisposable(ScopeProvider.UNBOUND))
        .subscribe(subscribers);
    firstSubscriber.assertNoValues();
    firstSubscriber.assertNoErrors();
    secondSubscriber.assertNoValues();
    secondSubscriber.assertNoErrors();
    rule.assertNoErrors();
  }

  @Test public void unbound_shouldStillPassValues() {
    TestSubscriber<Integer> firstSubscriber = new TestSubscriber<>();
    TestSubscriber<Integer> secondSubscriber = new TestSubscriber<>();
    PublishProcessor<Integer> source = PublishProcessor.create();
    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {firstSubscriber, secondSubscriber};

    source
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(ScopeProvider.UNBOUND))
        .subscribe(subscribers);

    source.onNext(1);
    source.onNext(2);
    firstSubscriber.assertValue(1);
    secondSubscriber.assertValue(2);
    firstSubscriber.dispose();
    secondSubscriber.dispose();
  }
}
