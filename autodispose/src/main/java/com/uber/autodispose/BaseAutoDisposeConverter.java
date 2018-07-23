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
import io.reactivex.functions.Function;
import java.util.concurrent.Callable;

import static com.uber.autodispose.AutoDisposeUtil.checkNotNull;

abstract class BaseAutoDisposeConverter {

  private static final Function<?, ?> IDENTITY_FUNCTION = new Function<Object, Object>() {
    @Override public Object apply(Object o) {
      return o;
    }
  };

  @Deprecated
  @SuppressWarnings("unchecked")
  static <T> Function<T, T> identityFunctionForGenerics() {
    return (Function<T, T>) IDENTITY_FUNCTION;
  }

  private final Maybe<?> scope;

  /**
   * Accepts a {@link ScopeProvider} for scope resolution.
   *
   * @param provider the {@link ScopeProvider}.
   */
  BaseAutoDisposeConverter(final ScopeProvider provider) {
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
  BaseAutoDisposeConverter(LifecycleScopeProvider<?> provider) {
    this(LifecycleScopes.deferredResolvedLifecycle(checkNotNull(provider, "provider == null")));
  }

  /**
   * Accepts a {@link Maybe} for scope resolution.
   *
   * @param scope the {@link Maybe}.
   */
  BaseAutoDisposeConverter(Maybe<?> scope) {
    this.scope = checkNotNull(scope, "scope == null");
  }

  protected Maybe<?> scope() {
    return scope;
  }
}
