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

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import java.util.concurrent.Callable;

import static com.uber.autodispose.AutoDisposeUtil.checkNotNull;

abstract class Scoper {

  private final Maybe<?> scope;

  /**
   * Accepts a {@link ScopeProvider} for scope resolution.
   *
   * @param provider the {@link ScopeProvider}.
   */
  Scoper(final ScopeProvider provider) {
    this(Maybe.defer(new Callable<MaybeSource<?>>() {
      @Override public MaybeSource<?> call() throws Exception {
        return provider.requestScope();
      }
    }));
  }

  /**
   * Accepts a {@link LifecycleScopeProvider} for scope resolution.
   *
   * @param provider the {@link LifecycleScopeProvider}.
   */
  Scoper(LifecycleScopeProvider<?> provider) {
    this(ScopeUtil.deferredResolvedLifecycle(checkNotNull(provider, "provider == null")));
  }

  /**
   * Accepts a {@link Maybe} for scope resolution.
   *
   * @param scope the {@link Maybe}.
   */
  Scoper(Maybe<?> scope) {
    this.scope = checkNotNull(scope, "scope == null");
  }

  protected Maybe<?> scope() {
    return scope;
  }
}
