/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2;

import io.reactivex.rxjava3.annotations.CheckReturnValue;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.reactivestreams.Subscriber;

/** Subscribe proxy that matches {@link Flowable}'s subscribe overloads. */
public interface FlowableSubscribeProxy<@NonNull T> {

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
  Disposable subscribe(
      Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete);

  /** Proxy for {@link Flowable#subscribe(Subscriber)}. */
  void subscribe(Subscriber<? super T> observer);

  /**
   * Proxy for {@link Flowable#subscribeWith(Subscriber)}.
   *
   * @return an {@link Subscriber}
   */
  @CheckReturnValue
  <@NonNull E extends Subscriber<? super T>> E subscribeWith(E observer);

  /**
   * Proxy for {@link Flowable#test()}.
   *
   * @return a {@link TestSubscriber}
   */
  @CheckReturnValue
  TestSubscriber<T> test();

  /**
   * Proxy for {@link Flowable#test(long)}.
   *
   * @return a {@link TestSubscriber}
   */
  @CheckReturnValue
  TestSubscriber<T> test(long initialRequest);

  /**
   * Proxy for {@link Flowable#test(long, boolean)}.
   *
   * @return a {@link TestSubscriber}
   */
  @CheckReturnValue
  TestSubscriber<T> test(long initialRequest, boolean cancel);
}
