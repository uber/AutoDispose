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
package autodispose2.observers;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * A {@link Disposable} {@link SingleObserver} that can automatically dispose itself. Interface here
 * for type safety but enforcement is left to the implementation.
 */
public interface AutoDisposingSingleObserver<@NonNull T> extends SingleObserver<T>, Disposable {

  /**
   * Returns the delegate {@link SingleObserver} that is used under the hood for introspection
   * purposes.
   */
  SingleObserver<? super T> delegateObserver();
}
