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

import com.uber.autodispose.observers.AutoDisposingCompletableObserver;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the around steps that match Flowable's subscribe overloads.
 */
public interface CompletableSubscribeClause {

  /**
   * Mirror for {@link Completable#subscribe()}
   *
   * @return an {@link AutoDisposingCompletableObserver}
   */
  AutoDisposingCompletableObserver empty();

  /**
   * Mirror for {@link Completable#subscribe(Action)}
   *
   * @return an {@link AutoDisposingCompletableObserver}
   */
  AutoDisposingCompletableObserver around(Action action);

  /**
   * Mirror for {@link Completable#subscribe(Action, Consumer)}
   *
   * @return an {@link AutoDisposingCompletableObserver}
   */
  AutoDisposingCompletableObserver around(Action action, Consumer<? super Throwable> onError);

  /**
   * Mirror for {@link Completable#subscribe(CompletableObserver)}
   *
   * @return an {@link AutoDisposingCompletableObserver}
   */
  AutoDisposingCompletableObserver around(CompletableObserver observer);
}
