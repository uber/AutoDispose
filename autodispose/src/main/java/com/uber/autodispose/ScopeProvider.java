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

package com.uber.autodispose;

import io.reactivex.Maybe;
import io.reactivex.annotations.CheckReturnValue;

/**
 * Proves a {@link Maybe} representation of a scope. The emission of this is the signal
 */
public interface ScopeProvider {

  /**
   * A new provider that is "unbound", e.g. will emit a completion event to signal that the
   * scope is unbound.
   */
  ScopeProvider UNBOUND = new ScopeProvider() {
    @Override public Maybe<?> requestScope() {
      return Maybe.empty();
    }
  };

  /**
   * @return a Maybe that, upon emission, will trigger disposal.
   */
  @CheckReturnValue Maybe<?> requestScope();
}
