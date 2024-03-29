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
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.TestObserver;

/** Subscribe proxy that matches {@link Observable}'s subscribe overloads. */
public interface ObservableSubscribeProxy<@NonNull T> {

  /**
   * Proxy for {@link Observable#subscribe()}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe();

  /**
   * Proxy for {@link Observable#subscribe(Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext);

  /**
   * Proxy for {@link Observable#subscribe(Consumer, Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError);

  /**
   * Proxy for {@link Observable#subscribe(Consumer, Consumer, Action)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(
      Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete);

  /** Proxy for {@link Observable#subscribe(Observer)}. */
  void subscribe(Observer<? super T> observer);

  /**
   * Proxy for {@link Observable#subscribeWith(Observer)}.
   *
   * @return an {@link Observer}
   */
  @CheckReturnValue
  <@NonNull E extends Observer<? super T>> E subscribeWith(E observer);

  /**
   * Proxy for {@link Observable#test()}.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue
  TestObserver<T> test();

  /**
   * Proxy for {@link Observable#test(boolean)}.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue
  TestObserver<T> test(boolean dispose);
}
