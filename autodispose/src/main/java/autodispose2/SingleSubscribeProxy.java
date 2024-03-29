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
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.TestObserver;

/** Subscribe proxy that matches {@link Single}'s subscribe overloads. */
public interface SingleSubscribeProxy<@NonNull T> {

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
  Disposable subscribe(BiConsumer<@Nullable ? super T, @Nullable ? super Throwable> biConsumer);

  /**
   * Proxy for {@link Single#subscribe(Consumer, Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError);

  /** Proxy for {@link Single#subscribe(SingleObserver)}. */
  void subscribe(SingleObserver<? super T> observer);

  /**
   * Proxy for {@link Single#subscribeWith(SingleObserver)}.
   *
   * @return a {@link SingleObserver}
   */
  @CheckReturnValue
  <@NonNull E extends SingleObserver<? super T>> E subscribeWith(E observer);

  /**
   * Proxy for {@link Single#test()}.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue
  TestObserver<T> test();

  /**
   * Proxy for {@link Single#test(boolean)}.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue
  TestObserver<T> test(boolean dispose);
}
