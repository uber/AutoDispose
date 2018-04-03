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

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.functions.Function;

/**
 * Entry point for auto-disposing {@link Completable}s.
 * <p>
 * The basic flow stencil might look like this:
 * <pre><code>
 *   myCompletable
 *        .to(new CompletableScoper...))
 *        .subscribe(...)
 * </code></pre>
 * <p>
 * There are several constructor overloads, with the most basic being a simple {@link
 * #CompletableScoper(Maybe)}. The provided {@link Maybe} is ultimately what every scope resolves to
 * under the hood, and AutoDispose has some built-in understanding for predefined types. The scope
 * is considered ended upon onSuccess emission of this {@link Maybe}. The most common use case would
 * probably be {@link #CompletableScoper(ScopeProvider)}.
 *
 * @deprecated Use the static factories in {@link AutoDispose}. This will be removed in 1.0.
 */
@Deprecated
public class CompletableScoper extends BaseAutoDisposeConverter implements
        Function<Completable, CompletableSubscribeProxy> {

  public CompletableScoper(ScopeProvider provider) {
    super(provider);
  }

  public CompletableScoper(LifecycleScopeProvider<?> provider) {
    super(provider);
  }

  public CompletableScoper(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override
  public CompletableSubscribeProxy apply(final Completable maybeSource) {
    return maybeSource.as(AutoDispose.autoDisposable(scope()));
  }
}
