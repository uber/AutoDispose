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

import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * A {@link Disposable} {@link CompletableObserver} that can automatically dispose itself.
 * Interface here for type safety but enforcement is left to the implementation.
 */
public interface AutoDisposingCompletableObserver extends CompletableObserver, Disposable {

    /**
     * @return The delegate {@link CompletableObserver} that is used under the hood for introspection purposes.
     */
    CompletableObserver delegateObserver();
}
