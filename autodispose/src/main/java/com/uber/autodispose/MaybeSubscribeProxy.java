/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.autodispose;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;

/** Subscribe proxy that matches {@link Maybe}'s subscribe overloads. */
public interface MaybeSubscribeProxy<T> {

  /**
   * Proxy for {@link Maybe#subscribe()}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe();

  /**
   * Proxy for {@link Maybe#subscribe(Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onSuccess);

  /**
   * Proxy for {@link Maybe#subscribe(Consumer, Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError);

  /**
   * Proxy for {@link Maybe#subscribe(Consumer, Consumer, Action)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(
      Consumer<? super T> onSuccess, Consumer<? super Throwable> onError, Action onComplete);

  /** Proxy for {@link Maybe#subscribe(MaybeObserver)}. */
  void subscribe(MaybeObserver<? super T> observer);

  /**
   * Proxy for {@link Maybe#subscribeWith(MaybeObserver)}.
   *
   * @return a {@link MaybeObserver}
   */
  @CheckReturnValue
  <E extends MaybeObserver<? super T>> E subscribeWith(E observer);

  /**
   * Proxy for {@link Maybe#test()}.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue
  TestObserver<T> test();

  /**
   * Proxy for {@link Maybe#test(boolean)}.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue
  TestObserver<T> test(boolean cancel);
}
