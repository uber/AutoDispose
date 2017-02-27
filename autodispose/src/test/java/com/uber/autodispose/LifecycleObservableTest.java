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

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Slimmed down version of RxJava's BehaviorSubject tests, tuned for testing {@link
 * LifecycleObservable}.
 */
public class LifecycleObservableTest {

  private final Throwable testException = new Throwable();

  @Test public void testThatSubscriberReceivesDefaultValueAndSubsequentEvents() {
    BehaviorSubject<String> subject = BehaviorSubject.createDefault("default");
    LifecycleObservable<String> lo = subject.to(LifecycleObservable.<String>converter());

    Observer<String> observer = mockObserver();
    lo.subscribe(observer);

    subject.onNext("one");
    subject.onNext("two");
    subject.onNext("three");

    verify(observer, times(1)).onNext("default");
    verify(observer, times(1)).onNext("one");
    verify(observer, times(1)).onNext("two");
    verify(observer, times(1)).onNext("three");
    verify(observer, never()).onError(testException);
    verify(observer, never()).onComplete();
  }

  @Test public void testThatSubscriberReceivesDefaultValueAndSubsequentEvents_publishing() {
    PublishSubject<String> subject = PublishSubject.create();
    LifecycleObservable<String> lo = subject.to(LifecycleObservable.<String>converter());

    Observer<String> observer = mockObserver();
    lo.subscribe(observer);

    subject.onNext("default");
    subject.onNext("one");
    subject.onNext("two");
    subject.onNext("three");

    verify(observer, times(1)).onNext("default");
    verify(observer, times(1)).onNext("one");
    verify(observer, times(1)).onNext("two");
    verify(observer, times(1)).onNext("three");
    verify(observer, never()).onError(testException);
    verify(observer, never()).onComplete();
  }

  @Test public void testThatSubscriberReceivesLatestAndThenSubsequentEvents() {
    BehaviorSubject<String> subject = BehaviorSubject.createDefault("default");
    LifecycleObservable<String> lo = subject.to(LifecycleObservable.<String>converter());

    subject.onNext("one");

    Observer<String> observer = mockObserver();
    lo.subscribe(observer);

    subject.onNext("two");
    subject.onNext("three");

    verify(observer, never()).onNext("default");
    verify(observer, times(1)).onNext("one");
    verify(observer, times(1)).onNext("two");
    verify(observer, times(1)).onNext("three");
    verify(observer, never()).onError(testException);
    verify(observer, never()).onComplete();
  }

  @Test public void testThatSubscriberReceivesLatestAndThenSubsequentEvents_publishing() {
    PublishSubject<String> subject = PublishSubject.create();
    LifecycleObservable<String> lo = subject.to(LifecycleObservable.<String>converter());

    subject.onNext("default");

    Observer<String> observer = mockObserver();
    lo.subscribe(observer);

    subject.onNext("one");
    subject.onNext("two");
    subject.onNext("three");

    verify(observer, never()).onNext("default");
    verify(observer, times(1)).onNext("one");
    verify(observer, times(1)).onNext("two");
    verify(observer, times(1)).onNext("three");
    verify(observer, never()).onError(testException);
    verify(observer, never()).onComplete();
  }

  @Test public void testSubscribeThenOnComplete() {
    BehaviorSubject<String> subject = BehaviorSubject.createDefault("default");
    LifecycleObservable<String> lo = subject.to(LifecycleObservable.<String>converter());

    Observer<String> observer = mockObserver();
    lo.subscribe(observer);

    subject.onNext("one");
    subject.onComplete();

    verify(observer, times(1)).onNext("default");
    verify(observer, times(1)).onNext("one");
    verify(observer, never()).onError(any(Throwable.class));
    verify(observer, times(1)).onComplete();
  }

  @Test public void testSubscribeToCompletedOnlyEmitsOnComplete() {
    BehaviorSubject<String> subject = BehaviorSubject.createDefault("default");
    LifecycleObservable<String> lo = subject.to(LifecycleObservable.<String>converter());
    subject.onNext("one");
    subject.onComplete();

    Observer<String> observer = mockObserver();
    lo.subscribe(observer);

    verify(observer, never()).onNext("default");
    verify(observer, never()).onNext("one");
    verify(observer, never()).onError(any(Throwable.class));
    verify(observer, times(1)).onComplete();
  }

  @Test public void testSubscribeToErrorOnlyEmitsOnError() {
    BehaviorSubject<String> subject = BehaviorSubject.createDefault("default");
    LifecycleObservable<String> lo = subject.to(LifecycleObservable.<String>converter());
    subject.onNext("one");
    RuntimeException re = new RuntimeException("test error");
    subject.onError(re);

    Observer<String> observer = mockObserver();
    lo.subscribe(observer);

    verify(observer, never()).onNext("default");
    verify(observer, never()).onNext("one");
    verify(observer, times(1)).onError(re);
    verify(observer, never()).onComplete();
  }

  @Test(timeout = 1000) public void testUnsubscriptionCase() {
    BehaviorSubject<String> src = BehaviorSubject.createDefault("null");
    LifecycleObservable<String> lo = src.to(LifecycleObservable.<String>converter());

    for (int i = 0; i < 10; i++) {
      final Observer<Object> o = mockObserver();
      InOrder inOrder = inOrder(o);
      String v = "" + i;
      src.onNext(v);
      System.out.printf("Turn: %d%n", i);
      lo.firstElement()
          .toObservable()
          .flatMap(new Function<String, Observable<String>>() {
            @Override public Observable<String> apply(String t1) {
              return Observable.just(t1 + ", " + t1);
            }
          })
          .subscribe(new DefaultObserver<String>() {
            @Override public void onNext(String t) {
              o.onNext(t);
            }

            @Override public void onError(Throwable e) {
              o.onError(e);
            }

            @Override public void onComplete() {
              o.onComplete();
            }
          });
      inOrder.verify(o)
          .onNext(v + ", " + v);
      inOrder.verify(o)
          .onComplete();
      verify(o, never()).onError(any(Throwable.class));
    }
  }

  @Test public void testCurrentStateMethodsNormalEmptyStart() {
    BehaviorSubject<Object> as = BehaviorSubject.create();
    LifecycleObservable<Object> lo = as.to(LifecycleObservable.converter());

    lo.subscribe();

    assertFalse(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertFalse(lo.hasComplete());
    assertNull(lo.getValue());
    assertNull(lo.getThrowable());

    as.onNext(1);

    assertTrue(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertFalse(lo.hasComplete());
    assertEquals(1, lo.getValue());
    assertNull(lo.getThrowable());

    as.onComplete();

    assertFalse(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertTrue(lo.hasComplete());
    assertNull(lo.getValue());
    assertNull(lo.getThrowable());
  }

  @Test public void testCurrentStateMethodsNormalSomeStart() {
    BehaviorSubject<Object> as = BehaviorSubject.createDefault((Object) 1);
    LifecycleObservable<Object> lo = as.to(LifecycleObservable.converter());
    lo.subscribe();

    assertTrue(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertFalse(lo.hasComplete());
    assertEquals(1, lo.getValue());
    assertNull(lo.getThrowable());

    as.onNext(2);

    assertTrue(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertFalse(lo.hasComplete());
    assertEquals(2, lo.getValue());
    assertNull(lo.getThrowable());

    as.onComplete();
    assertFalse(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertTrue(lo.hasComplete());
    assertNull(lo.getValue());
    assertNull(lo.getThrowable());
  }

  @Test public void testCurrentStateMethodsEmpty() {
    BehaviorSubject<Object> as = BehaviorSubject.create();
    LifecycleObservable<Object> lo = as.to(LifecycleObservable.converter());
    lo.subscribe();

    assertFalse(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertFalse(lo.hasComplete());
    assertNull(lo.getValue());
    assertNull(lo.getThrowable());

    as.onComplete();

    assertFalse(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertTrue(lo.hasComplete());
    assertNull(lo.getValue());
    assertNull(lo.getThrowable());
  }

  @Test public void testCurrentStateMethodsError() {
    BehaviorSubject<Object> as = BehaviorSubject.create();
    LifecycleObservable<Object> lo = as.to(LifecycleObservable.converter());
    lo.subscribe(LifecycleObservableTest.mockObserver());

    assertFalse(lo.hasValue());
    assertFalse(lo.hasThrowable());
    assertFalse(lo.hasComplete());
    assertNull(lo.getValue());
    assertNull(lo.getThrowable());

    as.onError(new TestException());

    assertFalse(lo.hasValue());
    assertTrue(lo.hasThrowable());
    assertFalse(lo.hasComplete());
    assertNull(lo.getValue());
    assertTrue(lo.getThrowable() instanceof TestException);
  }

  @Test public void cancelOnArrival() {
    BehaviorSubject<Object> p = BehaviorSubject.create();
    LifecycleObservable<Object> lo = p.to(LifecycleObservable.converter());

    assertFalse(p.hasObservers());

    lo.test(true)
        .assertEmpty();

    assertFalse(p.hasObservers());
  }

  @Test public void innerDisposed() {
    BehaviorSubject.create()
        .to(LifecycleObservable.converter())
        .subscribe(new Observer<Object>() {
          @Override public void onSubscribe(Disposable d) {
            assertFalse(d.isDisposed());

            d.dispose();

            assertTrue(d.isDisposed());
          }

          @Override public void onNext(Object value) {

          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onComplete() {

          }
        });
  }

  /**
   * Mocks an Observer with the proper receiver type.
   *
   * @param <T> the value type
   * @return the mocked observer
   */
  @SuppressWarnings("unchecked") private static <T> Observer<T> mockObserver() {
    return mock(Observer.class);
  }
}
