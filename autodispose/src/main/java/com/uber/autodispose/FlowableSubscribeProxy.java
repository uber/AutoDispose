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

import io.reactivex.Flowable;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.subscribers.TestSubscriber;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Subscribe proxy that matches {@link Flowable}'s subscribe overloads.
 */
public interface FlowableSubscribeProxy<T> {

  /**
   * Proxy for {@link Flowable#subscribe()}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe();

  /**
   * Proxy for {@link Flowable#subscribe(Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext);

  /**
   * Proxy for {@link Flowable#subscribe(Consumer, Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError);

  /**
   * Proxy for {@link Flowable#subscribe(Consumer, Consumer, Action)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
      Action onComplete);

  /**
   * Proxy for {@link Flowable#subscribe(Consumer, Consumer, Action, Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
      Action onComplete, Consumer<? super Subscription> onSubscribe);

  /**
   * Proxy for {@link Flowable#subscribe(Subscriber)}.
   */
  void subscribe(Subscriber<T> observer);

  /**
   * Proxy for {@link Flowable#subscribeWith(Subscriber)}.
   *
   * @return an {@link Subscriber}
   */
  @CheckReturnValue <E extends Subscriber<? super T>> E subscribeWith(E observer);

  /**
  * Proxy for {@link Flowable#test()}
  *
  * @return a {@link TestSubscriber}
  */
  @CheckReturnValue TestSubscriber<T> test();

  /**
  * Proxy for {@link Flowable#test(long)}
  *
  * @return a {@link TestSubscriber}
  */
  @CheckReturnValue TestSubscriber<T> test(long initialRequest);

  /**
   * Proxy for {@link Flowable#test(long, boolean)}
   *
   * @return a {@link TestSubscriber}
   */
  @CheckReturnValue TestSubscriber<T> test(long initialRequest, boolean cancel);
}
