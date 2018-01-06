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

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;

/**
 * Subscribe proxy that matches {@link Single}'s subscribe overloads.
 */
public interface SingleSubscribeProxy<T> {

  /**
   * Proxy for {@link Single#subscribe()}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe();

  /**
   * Proxy for {@link Single#subscribe(Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onSuccess);

  /**
   * Proxy for {@link Single#subscribe(BiConsumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(BiConsumer<? super T, ? super Throwable> biConsumer);

  /**
   * Proxy for {@link Single#subscribe(Consumer, Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError);

  /**
   * Proxy for {@link Single#subscribe(SingleObserver)}.
   */
  void subscribe(SingleObserver<T> observer);

  /**
   * Proxy for {@link Single#subscribeWith(SingleObserver)}.
   *
   * @return a {@link SingleObserver}
   */
  @CheckReturnValue <E extends SingleObserver<? super T>> E subscribeWith(E observer);

  /**
   * Creates a TestObserver and subscribes it to this Single.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue TestObserver<T> test();

  /**
   * Creates a TestObserver, optionally cancels it and then subscribes
   * it to this Single.
   *
   * @param cancel whether to cancel the TestObserver before it is subscribed to this Single
   * @return a {@link TestObserver}
   */
  @CheckReturnValue TestObserver<T> test(boolean cancel);
}
