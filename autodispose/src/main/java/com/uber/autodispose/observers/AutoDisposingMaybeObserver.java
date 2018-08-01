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

package com.uber.autodispose.observers;

import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;

/**
 * A {@link Disposable} {@link MaybeObserver} that can automatically dispose itself.
 * Interface here for type safety but enforcement is left to the implementation.
 */
public interface AutoDisposingMaybeObserver<T> extends MaybeObserver<T>, Disposable {

  /**
   * @return The delegate {@link MaybeObserver} that is used under the hood for introspection
   * purposes.
   */
  MaybeObserver<? super T> delegateObserver();
}
