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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.annotations.CheckReturnValue;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.TestObserver;

/** Subscribe proxy that matches {@link Completable}'s subscribe overloads. */
public interface CompletableSubscribeProxy {

  /**
   * Proxy for {@link Completable#subscribe()}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe();

  /**
   * Proxy for {@link Completable#subscribe(Action)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Action action);

  /**
   * Proxy for {@link Completable#subscribe(Action, Consumer)}.
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Action action, Consumer<? super Throwable> onError);

  /** Proxy for {@link Completable#subscribe(CompletableObserver)}. */
  void subscribe(CompletableObserver observer);

  /**
   * Proxy for {@link Completable#subscribeWith(CompletableObserver)}.
   *
   * @return a {@link CompletableObserver}
   */
  @CheckReturnValue
  <E extends CompletableObserver> E subscribeWith(E observer);

  /**
   * Proxy for {@link Completable#test()}.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue
  TestObserver<Void> test();

  /**
   * Proxy for {@link Completable#test(boolean)}.
   *
   * @return a {@link TestObserver}
   */
  @CheckReturnValue
  TestObserver<Void> test(boolean cancel);
}
