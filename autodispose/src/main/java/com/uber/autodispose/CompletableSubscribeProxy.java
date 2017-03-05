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

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Subscribe proxy that matches {@link Completable}'s subscribe overloads.
 */
public interface CompletableSubscribeProxy {

  /**
   * Proxy for {@link Completable#subscribe()}
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe();

  /**
   * Proxy for {@link Completable#subscribe(Action)}
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Action action);

  /**
   * Proxy for {@link Completable#subscribe(Action, Consumer)}
   *
   * @return a {@link Disposable}
   */
  Disposable subscribe(Action action, Consumer<? super Throwable> onError);

  /**
   * Proxy for {@link Completable#subscribe(CompletableObserver)}
   */
  void subscribe(CompletableObserver observer);

  /**
   * Proxy for {@link Completable#subscribeWith(CompletableObserver)}
   *
   * @return a {@link CompletableObserver}
   */
  <E extends CompletableObserver> E subscribeWith(E observer);
}
