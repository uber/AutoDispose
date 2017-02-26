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

import com.uber.autodispose.observers.AutoDisposingMaybeObserver;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored") public class LambdaMaybeObserverTest {

  @Rule public RxErrorsRule errors = new RxErrorsRule();

  private final Maybe<Integer> lifecycle = Maybe.empty();

  @Test public void onSubscribeThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingMaybeObserver<Object> o =
        new AutoDisposingMaybeObserverImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            received.add(o);
          }
        }, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            received.add(o);
          }
        }, new Action() {

          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Disposable>() {
          @Override public void accept(Disposable disposable) throws Exception {
            throw new TestException();
          }
        });

    assertFalse(o.isDisposed());

    Maybe.just(1)
        .subscribe(o);

    assertTrue(received.toString(), received.get(0) instanceof TestException);
    assertEquals(received.toString(), 1, received.size());

    assertTrue(o.isDisposed());
  }

  @Test public void onSuccessThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingMaybeObserver<Object> o =
        new AutoDisposingMaybeObserverImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            throw new TestException();
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable o) throws Exception {
            received.add(o);
          }
        }, new Action() {

          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Disposable>() {
          @Override public void accept(Disposable disposable) throws Exception {
            throw new TestException();
          }
        });

    assertFalse(o.isDisposed());

    Maybe.just(1)
        .subscribe(o);

    assertTrue(received.toString(), received.get(0) instanceof TestException);
    assertEquals(received.toString(), 1, received.size());

    assertTrue(o.isDisposed());
  }

  @Test public void onErrorThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingMaybeObserver<Object> o =
        new AutoDisposingMaybeObserverImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            throw new TestException();
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable o) throws Exception {
            throw new TestException("Inner");
          }
        }, new Action() {

          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Disposable>() {
          @Override public void accept(Disposable disposable) throws Exception {
          }
        });

    assertFalse(o.isDisposed());

    Maybe.<Integer>error(new TestException("Outer")).subscribe(o);

    assertTrue(received.toString(), received.isEmpty());

    assertTrue(o.isDisposed());

    CompositeException ex = errors.takeCompositeException();
    List<Throwable> ce = ex.getExceptions();
    assertThat(ce).hasSize(2);
    assertThat(ce.get(0)).hasMessage("Outer");
    assertThat(ce.get(1)).hasMessage("Inner");
  }

  @Test public void onCompleteThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingMaybeObserver<Object> o =
        new AutoDisposingMaybeObserverImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            received.add(o);
          }
        }, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            received.add(o);
          }
        }, new Action() {
          @Override public void run() throws Exception {
            throw new TestException();
          }
        }, new Consumer<Disposable>() {
          @Override public void accept(Disposable disposable) throws Exception {
          }
        });

    assertFalse(o.isDisposed());

    Maybe.<Integer>empty().subscribe(o);

    assertTrue(received.toString(), received.isEmpty());

    assertTrue(o.isDisposed());

    assertThat(errors.take()).isInstanceOf(TestException.class);
  }

  @Test @Ignore public void badSourceOnSubscribe() {
    Maybe<Integer> source = new Maybe<Integer>() {
      @Override public void subscribeActual(MaybeObserver<? super Integer> s) {
        Disposable s1 = Disposables.empty();
        s.onSubscribe(s1);
        Disposable s2 = Disposables.empty();
        s.onSubscribe(s2);

        assertFalse(s1.isDisposed());
        assertTrue(s2.isDisposed());

        s.onSuccess(1);
      }
    };

    final List<Object> received = new ArrayList<>();

    AutoDisposingMaybeObserver<Object> o =
        new AutoDisposingMaybeObserverImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            received.add(o);
          }
        }, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            received.add(o);
          }
        }, new Action() {

          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Disposable>() {
          @Override public void accept(Disposable disposable) throws Exception {

          }
        });

    source.subscribe(o);

    assertEquals(Arrays.asList(1, 100), received);
  }

  @Test public void badSourceEmitAfterDone() {
    Maybe<Integer> source = new Maybe<Integer>() {
      @Override public void subscribeActual(MaybeObserver<? super Integer> s) {
        s.onSubscribe(Disposables.empty());

        s.onComplete();
        s.onSuccess(2);
        s.onError(new TestException());
        s.onComplete();
      }
    };

    final List<Object> received = new ArrayList<>();

    AutoDisposingMaybeObserver<Object> o =
        new AutoDisposingMaybeObserverImpl<>(lifecycle, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            received.add(o);
          }
        }, new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            received.add(o);
          }
        }, new Action() {

          @Override public void run() throws Exception {
            received.add(100);
          }
        }, new Consumer<Disposable>() {
          @Override public void accept(Disposable disposable) throws Exception {
          }
        });

    source.subscribe(o);

    assertEquals(Collections.singletonList(100), received);
  }
}
