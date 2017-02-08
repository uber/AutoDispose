/*
 * Copyright (c) 2017. Uber Technologies
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
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An Observable whose behavior is similar to that of a {@link BehaviorSubject} but read-only, for
 * use with {@link LifecycleScopeProvider}.
 */
public final class LifecycleObservable<T> extends Observable<T> {

  private interface BehaviorFunction<T> extends Function<Observable<T>, LifecycleObservable<T>> {}

  private static final BehaviorFunction<?> BEHAVIOR_OBSERVABLE_FUNCTION =
      new BehaviorFunction<Object>() {
        @Override public LifecycleObservable<Object> apply(Observable<Object> upstream)
            throws Exception {
          return new LifecycleObservable<>(upstream);
        }
      };

  private final AtomicReference<Disposable> subjectDisposable = new AtomicReference<>();
  private volatile BehaviorSubject<T> subject = BehaviorSubject.create();
  private volatile Subject<T> serializedSubject = subject.toSerialized();
  private final Observable<T> actual;

  /**
   * Converts an Observable to a BehaviorObservable. Intended to be used with
   * {@link Observable#to(Function)}.
   *
   * <pre><code>
   *   BehaviorSubject.createDefault(1)
   *       .to(LifecycleObservable.converter())
   *       .subscribe();
   * </code></pre>
   *
   * @param <T> the stream type.
   * @return a converter function.
   */
  @SuppressWarnings("unchecked") public static <T> BehaviorFunction<T> converter() {
    return (BehaviorFunction<T>) BEHAVIOR_OBSERVABLE_FUNCTION;
  }

  private LifecycleObservable(Observable<T> actual) {
    this.actual = actual;
  }

  @Override protected void subscribeActual(Observer<? super T> observer) {
    // Subscribe the subject to the observer, track disposal so we can clear the behavior
    // subscription later.
    subject.subscribe(new LifecycleObserver(observer) {
      @Override void onDispose() {
        Disposable d = subjectDisposable.get();
        if (d != null && !d.isDisposed()) {
          d.dispose();
        }
      }
    });
    subjectDisposable.set(actual.subscribeWith(new DisposableObserver<T>() {
      @Override public void onNext(T t) {
        serializedSubject.onNext(t);
      }

      @Override public void onError(Throwable e) {
        serializedSubject.onError(e);
      }

      @Override public void onComplete() {
        serializedSubject.onComplete();
      }
    }));
  }

  /**
   * Passthrough to {@link BehaviorSubject#getThrowable()}
   */
  public Throwable getThrowable() {
    return subject.getThrowable();
  }

  /**
   * Passthrough to {@link BehaviorSubject#getValue()}
   */
  public T getValue() {
    return subject.getValue();
  }

  /**
   * Passthrough to {@link BehaviorSubject#getValues()}
   */
  public Object[] getValues() {
    return subject.getValues();
  }

  /**
   * Passthrough to {@link BehaviorSubject#hasComplete()}
   */
  public boolean hasComplete() {
    return subject.hasComplete();
  }

  /**
   * Passthrough to {@link BehaviorSubject#hasThrowable()}
   */
  public boolean hasThrowable() {
    return subject.hasThrowable();
  }

  /**
   * Passthrough to {@link BehaviorSubject#getValues(Object[])}
   */
  public T[] getValues(T[] array) {
    return subject.getValues(array);
  }

  /**
   * Passthrough to {@link BehaviorSubject#hasValue()}
   */
  public boolean hasValue() {
    return subject.hasValue();
  }

  abstract class LifecycleObserver implements Observer<T>, Disposable {

    private final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
    final Observer<? super T> actual;

    private LifecycleObserver(Observer<? super T> actual) {
      this.actual = actual;
    }

    @Override public void onNext(T t) {
      actual.onNext(t);
    }

    @Override public void onError(Throwable e) {
      actual.onError(e);
    }

    @Override public void onComplete() {
      actual.onComplete();
    }

    @Override public void dispose() {
      AutoDisposableHelper.dispose(disposableRef);
      onDispose();
    }

    @Override public boolean isDisposed() {
      return AutoDisposableHelper.isDisposed(disposableRef.get());
    }

    abstract void onDispose();

    @Override public void onSubscribe(Disposable d) {
      if (AutoDisposableHelper.setOnce(disposableRef, d)) {
        actual.onSubscribe(this);
      }
    }
  }
}
