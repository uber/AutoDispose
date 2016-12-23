/**
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

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.exceptions.CompositeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class LambdaObserverTest {

  @Rule public RxErrorsRule errors = new RxErrorsRule();

  private final Maybe<Integer> lifecycle = Maybe.empty();

  @Test public void onSubscribeThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingObserver<Object> o =
        new AutoDisposingObserver<>(lifecycle, received::add, received::add,
            () -> received.add(100), s -> {
          throw new TestException();
        });

    assertFalse(o.isDisposed());

    Observable.just(1).subscribe(o);

    assertTrue(received.toString(), received.get(0) instanceof TestException);
    assertEquals(received.toString(), 1, received.size());

    assertTrue(o.isDisposed());
  }

  @Test public void onNextThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingObserver<Object> o = new AutoDisposingObserver<>(lifecycle, v -> {
      throw new TestException();
    }, received::add, () -> received.add(100), s -> {
    });

    assertFalse(o.isDisposed());

    Observable.just(1).subscribe(o);

    assertTrue(received.toString(), received.get(0) instanceof TestException);
    assertEquals(received.toString(), 1, received.size());

    assertTrue(o.isDisposed());
  }

  @Test public void onErrorThrows() {
    final List<Object> received = new ArrayList<>();

    AutoDisposingObserver<Object> o = new AutoDisposingObserver<>(lifecycle, received::add, e -> {
      throw new TestException("Inner");
    }, () -> received.add(100), s -> {
    });

    assertFalse(o.isDisposed());

    Observable.<Integer>error(new TestException("Outer")).subscribe(o);

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

    AutoDisposingObserver<Object> o =
        new AutoDisposingObserver<>(lifecycle, received::add, received::add, () -> {
          throw new TestException();
        }, s -> {
        });

    assertFalse(o.isDisposed());

    Observable.<Integer>empty().subscribe(o);

    assertTrue(received.toString(), received.isEmpty());

    assertTrue(o.isDisposed());

    assertThat(errors.take()).isInstanceOf(TestException.class);
  }

  @Test public void badSourceOnSubscribe() {
    Observable<Integer> source = new Observable<Integer>() {
      @Override public void subscribeActual(Observer<? super Integer> s) {
        Disposable s1 = Disposables.empty();
        s.onSubscribe(s1);
        Disposable s2 = Disposables.empty();
        s.onSubscribe(s2);

        assertFalse(s1.isDisposed());
        assertTrue(s2.isDisposed());

        s.onNext(1);
        s.onComplete();
      }
    };

    final List<Object> received = new ArrayList<>();

    AutoDisposingObserver<Object> o =
        new AutoDisposingObserver<>(lifecycle, received::add, received::add,
            () -> received.add(100), s -> {});

    source.subscribe(o);

    assertEquals(Arrays.asList(1, 100), received);
  }

  @Test public void badSourceEmitAfterDone() {
    Observable<Integer> source = new Observable<Integer>() {
      @Override public void subscribeActual(Observer<? super Integer> s) {
        s.onSubscribe(Disposables.empty());

        s.onNext(1);
        s.onComplete();
        s.onNext(2);
        s.onError(new TestException());
        s.onComplete();
      }
    };

    final List<Object> received = new ArrayList<>();

    AutoDisposingObserver<Object> o =
        new AutoDisposingObserver<>(lifecycle, received::add, received::add,
            () -> received.add(100), s -> {});

    source.subscribe(o);

    assertEquals(Arrays.asList(1, 100), received);
  }

  private static class TestException extends RuntimeException {
    TestException() {
      super();
    }

    TestException(String s) {
      super(s);
    }
  }
}
