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
package autodispose2.lifecycle;

import autodispose2.ScopeProvider;
import com.google.errorprone.annotations.DoNotMock;
import io.reactivex.rxjava3.annotations.CheckReturnValue;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Observable;

/**
 * A convenience interface that, when implemented, helps provide information to create {@link
 * ScopeProvider} implementations that resolve the next corresponding lifecycle event and construct
 * a {@link Completable} representation of it from the {@link #lifecycle()} stream.
 *
 * <p>Convenience resolver utilities for this can be found in {@link LifecycleScopes}.
 *
 * @param <E> the lifecycle event type.
 * @see LifecycleScopes
 */
@DoNotMock(value = "Use TestLifecycleScopeProvider instead")
public interface LifecycleScopeProvider<@NonNull E> extends ScopeProvider {

  /**
   * Returns a sequence of lifecycle events. Note that completion of this lifecycle will also
   * trigger disposal
   */
  @CheckReturnValue
  Observable<E> lifecycle();

  /**
   * Returns a sequence of lifecycle events. It's recommended to back this with a static instance to
   * avoid unnecessary object allocation.
   */
  @CheckReturnValue
  CorrespondingEventsFunction<E> correspondingEvents();

  /**
   * Returns the last seen lifecycle event, or {@code null} if none. Note that is {@code null} is
   * returned at subscribe-time, it will be used as a signal to throw a {@link
   * LifecycleNotStartedException}.
   */
  @Nullable
  E peekLifecycle();

  @Override
  default CompletableSource requestScope() {
    return LifecycleScopes.resolveScopeFromLifecycle(this);
  }
}
