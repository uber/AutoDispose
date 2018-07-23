package com.uber.autodispose;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subscribers.TestSubscriber;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Subscriber;

import static com.google.common.truth.Truth.assertThat;

public class AutoDisposeParallelFlowableTest {

  private static final int DEFAULT_PARALLELISM = 2;

  @Rule public final RxErrorsRule rule = new RxErrorsRule();

  @Test public void ifParallelism_and_subscribersCount_dontMatch_shouldFail() {
    TestSubscriber<Integer> subscriber = new TestSubscriber<>();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();

    //noinspection unchecked
    Subscriber<Integer>[] subscribers = new Subscriber[] {subscriber};
    Flowable.just(1, 2)
        .parallel(DEFAULT_PARALLELISM)
        .as(AutoDispose.<Integer>autoDisposable(lifecycle))
        .subscribe(subscribers);

    List<Throwable> errors = subscriber.errors();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).isInstanceOf(IllegalArgumentException.class);
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
