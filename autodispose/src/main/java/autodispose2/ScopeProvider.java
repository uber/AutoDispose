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

import com.google.errorprone.annotations.DoNotMock;
import io.reactivex.rxjava3.annotations.CheckReturnValue;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;

/**
 * Provides a {@link CompletableSource} representation of a scope. The emission of this is the
 * signal
 */
@DoNotMock(value = "Use TestScopeProvider instead")
public interface ScopeProvider {

  /**
   * A new provider that is "unbound", e.g. will emit a completion event to signal that the scope is
   * unbound.
   */
  ScopeProvider UNBOUND = Completable::never;

  /**
   * Returns a {@link CompletableSource} that, upon completion, will trigger disposal.
   *
   * @throws Exception scope retrievals throws an exception, such as {@link OutsideScopeException}
   */
  @CheckReturnValue
  CompletableSource requestScope() throws Exception;
}
