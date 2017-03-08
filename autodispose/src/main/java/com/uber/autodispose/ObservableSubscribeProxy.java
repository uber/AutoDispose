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
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Subscribe proxy that matches {@link Observable}'s subscribe overloads.
 */
public interface ObservableSubscribeProxy<T> {

  /**
   * Proxy for {@link Observable#subscribe()}
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe();

  /**
   * Proxy for {@link Observable#subscribe(Consumer)}
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext);

  /**
   * Proxy for {@link Observable#subscribe(Consumer, Consumer)}
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError);

  /**
   * Proxy for {@link Observable#subscribe(Consumer, Consumer, Action)}
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
      Action onComplete);

  /**
   * Proxy for {@link Observable#subscribe(Consumer, Consumer, Action, Consumer)}
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
      Action onComplete,
      Consumer<? super Disposable> onSubscribe);

  /**
   * Proxy for {@link Observable#subscribe(Observer)}
   */
  void subscribe(Observer<T> observer);

  /**
   * Proxy for {@link Observable#subscribeWith(Observer)}
   *
   * @return an {@link Observer}
   */
  <E extends Observer<? super T>> E subscribeWith(E observer);
}
