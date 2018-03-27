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

  /**
   * Helper method for returning a generically typed {@link Function<T>} reusing the singleton
   * {@link #IDENTITY_FUNCTION} instance. This is NOT considered public API, and only here so that
   * migrated Scopers can safely cast to the new as() converter APIs.
   *
   * @param <T> the type parameter.
   * @return the casted identity function.
   * @deprecated Deprecated from inception as this is just temporary cover for deprecated Scopers
   *             migrating to the new as() APIs.
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  protected static <T> Function<T, T> identityFunctionForGenerics() {
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
    this(ScopeUtil.deferredResolvedLifecycle(checkNotNull(provider, "provider == null")));
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
