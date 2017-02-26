/*
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.uber.autodispose;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.subscriptions.BooleanSubscription;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LambdaSubscriberTest {

  @Rule public RxErrorsRule errors = new RxErrorsRule();

  private final Maybe<?> lifecycle = Maybe.empty();

  @Test public void onSubscribeThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingSubscriberImpl<Object> o =
        new AutoDisposingSubscriberImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object v) throws Exception {
            received.add(v);
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable e) throws Exception {
            received.add(e);
          }
        }, new Action() {
          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Subscription>() {
          @Override public void accept(Subscription s) throws Exception {
            throw new TestException();
          }
        });

    assertFalse(o.isDisposed());

    Flowable.just(1)
        .subscribe(o);

    assertTrue(received.toString(), received.get(0) instanceof TestException);
    assertEquals(received.toString(), 1, received.size());

    assertTrue(o.isDisposed());
  }

  @Test public void onNextThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingSubscriberImpl<Object> o =
        new AutoDisposingSubscriberImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object v) throws Exception {
            throw new TestException();
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable e) throws Exception {
            received.add(e);
          }
        }, new Action() {
          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Subscription>() {
          @Override public void accept(Subscription s) throws Exception {
            s.request(Long.MAX_VALUE);
          }
        });

    assertFalse(o.isDisposed());

    Flowable.just(1)
        .subscribe(o);

    assertTrue(received.toString(), received.get(0) instanceof TestException);
    assertEquals(received.toString(), 1, received.size());

    assertTrue(o.isDisposed());
  }

  @Test public void onErrorThrows() {
    try {
      final List<Object> received = new ArrayList<>();

      AutoDisposingSubscriberImpl<Object> o =
          new AutoDisposingSubscriberImpl<>(lifecycle, new Consumer<Object>() {
            @Override public void accept(Object v) throws Exception {
              received.add(v);
            }
          }, new Consumer<Throwable>() {
            @Override public void accept(Throwable e) throws Exception {
              throw new TestException("Inner");
            }
          }, new Action() {
            @Override public void run() throws Exception {
              received.add(100);
            }
          }, new Consumer<Subscription>() {
            @Override public void accept(Subscription s) throws Exception {
              s.request(Long.MAX_VALUE);
            }
          });

      assertFalse(o.isDisposed());

      Flowable.<Integer>error(new TestException("Outer")).subscribe(o);

      assertTrue(received.toString(), received.isEmpty());

      assertTrue(o.isDisposed());

      CompositeException ex = errors.takeCompositeException();
      List<Throwable> ce = ex.getExceptions();
      assertThat(ce).hasSize(2);
      assertThat(ce.get(0)).hasMessage("Outer");
      assertThat(ce.get(1)).hasMessage("Inner");
    } finally {
      RxJavaPlugins.reset();
    }
  }

  @Test public void onCompleteThrows() {
    try {
      final List<Object> received = new ArrayList<>();

      AutoDisposingSubscriberImpl<Object> o =
          new AutoDisposingSubscriberImpl<>(lifecycle, new Consumer<Object>() {
            @Override public void accept(Object v) throws Exception {
              received.add(v);
            }
          }, new Consumer<Throwable>() {
            @Override public void accept(Throwable e) throws Exception {
              received.add(e);
            }
          }, new Action() {
            @Override public void run() throws Exception {
              throw new TestException();
            }
          }, new Consumer<Subscription>() {
            @Override public void accept(Subscription s) throws Exception {
              s.request(Long.MAX_VALUE);
            }
          });

      assertFalse(o.isDisposed());

      Flowable.<Integer>empty().subscribe(o);

      assertTrue(received.toString(), received.isEmpty());

      assertTrue(o.isDisposed());

      assertThat(errors.take()).isInstanceOf(TestException.class);
    } finally {
      RxJavaPlugins.reset();
    }
  }

  @Test @Ignore public void badSourceOnSubscribe() {
    Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {
      @Override public void subscribe(Subscriber<? super Integer> s) {
        BooleanSubscription s1 = new BooleanSubscription();
        s.onSubscribe(s1);
        BooleanSubscription s2 = new BooleanSubscription();
        s.onSubscribe(s2);

        assertFalse(s1.isCancelled());
        assertTrue(s2.isCancelled());

        s.onNext(1);
        s.onComplete();
      }
    });

    final List<Object> received = new ArrayList<>();

    AutoDisposingSubscriberImpl<Object> o =
        new AutoDisposingSubscriberImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object v) throws Exception {
            received.add(v);
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable e) throws Exception {
            received.add(e);
          }
        }, new Action() {
          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Subscription>() {
          @Override public void accept(Subscription s) throws Exception {
            s.request(Long.MAX_VALUE);
          }
        });

    source.subscribe(o);

    assertEquals(Arrays.asList(1, 100), received);
  }

  @Test public void badSourceEmitAfterDone() {
    Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {
      @Override public void subscribe(Subscriber<? super Integer> s) {
        BooleanSubscription s1 = new BooleanSubscription();
        s.onSubscribe(s1);

        s.onNext(1);
        s.onComplete();
        s.onNext(2);
        s.onError(new TestException());
        s.onComplete();
      }
    });

    final List<Object> received = new ArrayList<>();

    AutoDisposingSubscriberImpl<Object> o =
        new AutoDisposingSubscriberImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object v) throws Exception {
            received.add(v);
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable e) throws Exception {
            received.add(e);
          }
        }, new Action() {
          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Subscription>() {
          @Override public void accept(Subscription s) throws Exception {
            s.request(Long.MAX_VALUE);
          }
        });

    source.subscribe(o);

    assertEquals(Arrays.asList(1, 100), received);
  }
}
