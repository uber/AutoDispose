/*
 * Copyright (C) 2018. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uber.autodispose.lifecycle.jdk8;

import com.uber.autodispose.lifecycle.LifecycleScopeProvider;
import com.uber.autodispose.lifecycle.LifecycleScopes;
import io.reactivex.Completable;

/**
 * A convenience {@link LifecycleScopeProvider} that has a default implementation for
 * {@link #requestScope()}.
 *
 * @param <E> the lifecycle event type.
 */
public interface DefaultLifecycleScopeProvider<E> extends LifecycleScopeProvider<E> {

  @Override default Completable requestScope() {
    return LifecycleScopes.resolveScopeFromLifecycle(this);
  }
}
