/*
 * Copyright (c) 2018. Uber Technologies
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
import io.reactivex.Maybe;

final class AutoDisposeCompletable extends Completable {

  private final Completable source;
  private final Maybe<?> scope;

  AutoDisposeCompletable(Completable source, Maybe<?> scope) {
    this.source = source;
    this.scope = scope;
  }

  @Override
  protected void subscribeActual(CompletableObserver completableObserver) {
    source.subscribe(new AutoDisposingCompletableObserverImpl(scope, completableObserver));
  }
}
