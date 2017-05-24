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

package com.uber.autodispose.android;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.LifecycleNotStartedException;
import com.uber.autodispose.LifecycleScopeProvider;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;

/**
 * A {@link LifecycleScopeProvider} that can provide scoping for Android
 * {@link LifecycleOwner} classes.
 * <p>
 * <pre><code>
 *   AutoDispose.observable()
 *      .scopeWith(LifecycleOwnerScopeProvider.from(view))
 *      .empty();
 * </code></pre>
 */
public final class LifecycleOwnerScopeProvider implements LifecycleScopeProvider<Lifecycle.State> {

  private static final Function<Lifecycle.State, Lifecycle.State> CORRESPONDING_EVENTS =
      new Function<Lifecycle.State, Lifecycle.State>() {
        @Override public Lifecycle.State apply(Lifecycle.State lastEvent) throws Exception {
          switch (lastEvent) {
            case INITIALIZED:
              throw new LifecycleNotStartedException();
            case CREATED:
              return DESTROYED;
            case STARTED:
              return DESTROYED;
            case RESUMED:
              return DESTROYED;
            default:
              throw new LifecycleEndedException();
          }
        }
      };

  /**
   * Creates a {@link LifecycleOwnerScopeProvider} for Android LifecycleOwners.
   *
   * @param owner the owner to scope for
   * @return a {@link LifecycleOwnerScopeProvider} against this owner.
   */
  public static LifecycleOwnerScopeProvider from(LifecycleOwner owner) {
    return from(owner.getLifecycle());
  }

  /**
   * Creates a {@link LifecycleOwnerScopeProvider} for Android Lifecycles.
   *
   * @param lifecycle the lifecycle to scope for
   * @return a {@link LifecycleOwnerScopeProvider} against this lifecycle.
   */
  public static LifecycleOwnerScopeProvider from(Lifecycle lifecycle) {
    return new LifecycleOwnerScopeProvider(lifecycle);
  }

  private final Lifecycle lifecycle;
  private final LifecycleStatesObservable lifecycleObservable;

  private LifecycleOwnerScopeProvider(Lifecycle lifecycle) {
    this.lifecycle = lifecycle;
    this.lifecycleObservable = new LifecycleStatesObservable(lifecycle);
  }

  @Override public Observable<Lifecycle.State> lifecycle() {
    return lifecycleObservable;
  }

  @Override public Function<Lifecycle.State, Lifecycle.State> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Override public Lifecycle.State peekLifecycle() {
    return lifecycle.getCurrentState();
  }
}
