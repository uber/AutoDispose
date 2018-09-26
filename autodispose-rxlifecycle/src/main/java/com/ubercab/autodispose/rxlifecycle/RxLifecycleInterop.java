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

package com.ubercab.autodispose.rxlifecycle;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.uber.autodispose.ScopeProvider;

/**
 * Interop for RxLifecycle. This provides static factory methods to convert {@link
 * LifecycleProvider}s into {@link ScopeProvider} representations.
 *
 * <em>Note:</em> RxLifecycle treats the {@link OutsideLifecycleException}
 * as normal terminal event. In such cases the stream is just disposed.
 */
public final class RxLifecycleInterop {

  private RxLifecycleInterop() {
    throw new AssertionError("No Instances");
  }

  /**
   * Factory creating a {@link ScopeProvider} representation of a {@link LifecycleProvider}.
   * <p>
   * Example usage:
   * <pre><code>
   *   Observable.just(1)
   *        .to(AutoDispose.with(RxLifecycleInterop.from(lifecycleProvider)).forObservable())
   *        .subscribe(...)
   * </code></pre>
   *
   * @param <E> the lifecycle event.
   * @param provider the {@link LifecycleProvider}.
   * @return a {@link ScopeProvider}
   */
  public static <E> ScopeProvider from(final LifecycleProvider<E> provider) {
    return () -> provider.lifecycle()
        .compose(provider.bindToLifecycle())
        .ignoreElements();
  }

  /**
   * Factory creating a {@link ScopeProvider} representation of a {@link LifecycleProvider}.
   * <p>
   * Example usage:
   * <pre><code>
   *   Observable.just(1)
   *        .to(AutoDispose.with(RxLifecycleInterop.from(lifecycleProvider, event)).forObservable())
   *        .subscribe(...)
   * </code></pre>
   *
   * @param <E> the lifecycle event.
   * @param provider the {@link LifecycleProvider}.
   * @param event a target event to dispose upon.
   * @return a {@link ScopeProvider}
   */
  public static <E> ScopeProvider from(final LifecycleProvider<E> provider, final E event) {
    return () -> provider.lifecycle()
        .compose(provider.bindUntilEvent(event))
        .ignoreElements();
  }
}
