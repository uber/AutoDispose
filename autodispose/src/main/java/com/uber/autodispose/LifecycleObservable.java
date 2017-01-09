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
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

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
    actual.subscribe(observer);
  }
}
