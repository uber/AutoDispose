package com.uber.autodispose;

import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.SingleSubject;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class OverloadsTest {

  @Test
  public void publisher_takeFirst() {
    PublishProcessor<Integer> source = PublishProcessor.create();
    PublishProcessor<Integer> scope = PublishProcessor.create();
    TestSubscriber<Integer> o = source.as(AutoDispose.<Integer, Integer>autoDisposable(scope, true))
        .test();
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasSubscribers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onNext(1);
    source.onNext(2);

    // No more events
    o.assertValue(1);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasSubscribers()).isFalse();
  }

  @Test
  public void publisher_predicate() {
    PublishProcessor<Integer> source = PublishProcessor.create();
    PublishProcessor<Integer> scope = PublishProcessor.create();
    TestSubscriber<Integer> o = source.as(AutoDispose.<Integer, Integer>autoDisposable(scope,
        new Predicate<Integer>() {
          @Override public boolean test(Integer i) {
            // Dispose when this emits 1
            return i == 1;
          }
        }))
        .test();
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasSubscribers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onNext(0);
    source.onNext(2);
    o.assertValues(1, 2);

    scope.onNext(1);
    source.onNext(3);

    // No more events
    o.assertValues(1, 2);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasSubscribers()).isFalse();
  }

  @Test
  public void publisher_takeFirst_complete() {
    PublishProcessor<Integer> source = PublishProcessor.create();
    PublishProcessor<Integer> scope = PublishProcessor.create();
    TestSubscriber<Integer> o = source.as(AutoDispose.<Integer, Integer>autoDisposable(scope, true))
        .test();
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasSubscribers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onComplete();
    source.onNext(2);

    // No more events
    o.assertValue(1);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasSubscribers()).isFalse();
  }

  @Test
  public void publisher_predicate_complete() {
    PublishProcessor<Integer> source = PublishProcessor.create();
    PublishProcessor<Integer> scope = PublishProcessor.create();
    TestSubscriber<Integer> o = source.as(AutoDispose.<Integer, Integer>autoDisposable(scope,
        new Predicate<Integer>() {
          @Override public boolean test(Integer i) {
            // Dispose when this emits 1
            return i == 1;
          }
        }))
        .test();
    o.assertSubscribed();

    assertThat(source.hasSubscribers()).isTrue();
    assertThat(scope.hasSubscribers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onNext(0);
    source.onNext(2);
    o.assertValues(1, 2);

    scope.onComplete();
    source.onNext(3);

    // No more events
    o.assertValues(1, 2);

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse();
    assertThat(scope.hasSubscribers()).isFalse();
  }

  @Test
  public void observable_takeFirst() {
    PublishSubject<Integer> source = PublishSubject.create();
    PublishSubject<Integer> scope = PublishSubject.create();
    TestObserver<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(scope, true))
        .test();
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onNext(1);
    source.onNext(2);

    // No more events
    o.assertValue(1);

    // Unsubscribed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void observable_predicate() {
    PublishSubject<Integer> source = PublishSubject.create();
    PublishSubject<Integer> scope = PublishSubject.create();
    TestObserver<Integer> o = source.as(AutoDispose.<Integer, Integer>autoDisposable(scope,
        new Predicate<Integer>() {
          @Override public boolean test(Integer i) {
            // Dispose when this emits 1
            return i == 1;
          }
        }))
        .test();
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onNext(0);
    source.onNext(2);
    o.assertValues(1, 2);

    scope.onNext(1);
    source.onNext(3);

    // No more events
    o.assertValues(1, 2);

    // Unsubscribed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void observable_takeFirst_complete() {
    PublishSubject<Integer> source = PublishSubject.create();
    PublishSubject<Integer> scope = PublishSubject.create();
    TestObserver<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(scope, true))
        .test();
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onComplete();
    source.onNext(2);

    // No more events
    o.assertValue(1);

    // Unsubscribed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void observable_predicate_complete() {
    PublishSubject<Integer> source = PublishSubject.create();
    PublishSubject<Integer> scope = PublishSubject.create();
    TestObserver<Integer> o = source.as(AutoDispose.<Integer, Integer>autoDisposable(scope,
        new Predicate<Integer>() {
          @Override public boolean test(Integer i) {
            // Dispose when this emits 1
            return i == 1;
          }
        }))
        .test();
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    scope.onNext(0);
    source.onNext(2);
    o.assertValues(1, 2);

    scope.onComplete();
    source.onNext(3);

    // No more events
    o.assertValues(1, 2);

    // Unsubscribed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void single() {
    SingleSubject<Integer> source = SingleSubject.create();
    SingleSubject<Integer> scope = SingleSubject.create();
    TestObserver<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(scope))
        .test();
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    scope.onSuccess(1);

    // No more events
    o.assertNoValues();

    // Unsubscribed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void maybe() {
    MaybeSubject<Integer> source = MaybeSubject.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    TestObserver<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(scope))
        .test();
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    scope.onSuccess(1);

    // No more events
    o.assertNoValues();

    // Unsubscribed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void maybe_complete() {
    MaybeSubject<Integer> source = MaybeSubject.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    TestObserver<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(scope))
        .test();
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    scope.onComplete();

    // No more events
    o.assertNoValues();

    // Unsubscribed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

}
