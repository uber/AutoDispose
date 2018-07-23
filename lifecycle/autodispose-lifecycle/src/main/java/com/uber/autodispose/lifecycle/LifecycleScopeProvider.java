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

package com.uber.autodispose.lifecycle;

import com.uber.autodispose.ScopeProvider;
import com.uber.autodispose.internal.DoNotMock;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.Nullable;

/**
 * A convenience interface that, when implemented, helps provide information to create {@link
 * ScopeProvider} implementations that resolve the next corresponding lifecycle event and construct
 * a {@link Maybe} representation of it from the {@link #lifecycle()} stream.
 *
 * <p>Convenience resolver utilities for this can be found in {@link LifecycleScopes}.
 *
 * @param <E> the lifecycle event type.
 * @see LifecycleScopes
 */
@DoNotMock(value = "Use TestLifecycleScopeProvider instead")
public interface LifecycleScopeProvider<E> extends ScopeProvider {

  /** @return a sequence of lifecycle events. */
  @CheckReturnValue Observable<E> lifecycle();

  /**
   * @return a sequence of lifecycle events. It's recommended to back this with a static instance to
   *     avoid unnecessary object allocation.
   */
  @CheckReturnValue Function<E, E> correspondingEvents();

  /** @return the last seen lifecycle event, or {@code null} if none. */
  @Nullable E peekLifecycle();
}
