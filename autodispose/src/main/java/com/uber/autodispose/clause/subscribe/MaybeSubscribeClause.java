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

package com.uber.autodispose.clause.subscribe;

import com.uber.autodispose.observers.AutoDisposingMaybeObserver;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the around steps that match Maybe's subscribe overloads.
 */
public interface MaybeSubscribeClause {

  /**
   * Proxy for {@link Maybe#subscribe()}
   *
   * @param <T> the Maybe type
   * @return an {@link AutoDisposingMaybeObserver}
   */
  <T> AutoDisposingMaybeObserver<T> empty();

  /**
   * Proxy for {@link Maybe#subscribe(Consumer)}
   *
   * @param <T> the Maybe type
   * @return an {@link AutoDisposingMaybeObserver}
   */
  <T> AutoDisposingMaybeObserver<T> around(Consumer<? super T> onSuccess);

  /**
   * Proxy for {@link Maybe#subscribe(Consumer, Consumer)}
   *
   * @param <T> the Maybe type
   * @return an {@link AutoDisposingMaybeObserver}
   */
  <T> AutoDisposingMaybeObserver<T> around(Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError);

  /**
   * Proxy for {@link Maybe#subscribe(Consumer, Consumer, Action)}
   *
   * @param <T> the Maybe type
   * @return an {@link AutoDisposingMaybeObserver}
   */
  <T> AutoDisposingMaybeObserver<T> around(Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError,
      Action onComplete);

  /**
   * Proxy for {@link Maybe#subscribe(MaybeObserver)}
   *
   * @param <T> the Maybe type
   * @return an {@link AutoDisposingMaybeObserver}
   */
  <T> AutoDisposingMaybeObserver<T> around(MaybeObserver<T> observer);
}
