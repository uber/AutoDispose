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

package com.uber.autodispose.android.androidlifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.LifecycleScopeProvider;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * A {@link LifecycleScopeProvider} that can provide scoping for Android {@link Lifecycle} and
 * {@link LifecycleOwner} classes.
 * <p>
 * <pre><code>
 *   AutoDispose.with(AndroidLifecycleScopeProvider.from(lifecycleOwner))
 * </code></pre>
 */
public final class AndroidLifecycleScopeProvider
    implements LifecycleScopeProvider<Lifecycle.Event> {

  private static final Function<Lifecycle.Event, Lifecycle.Event> CORRESPONDING_EVENTS =
      new Function<Lifecycle.Event, Lifecycle.Event>() {
        @Override public Lifecycle.Event apply(Lifecycle.Event lastEvent) throws Exception {
          switch (lastEvent) {
            case ON_CREATE:
              return Lifecycle.Event.ON_DESTROY;
            case ON_START:
              return Lifecycle.Event.ON_STOP;
            case ON_RESUME:
              return Lifecycle.Event.ON_PAUSE;
            case ON_PAUSE:
              return Lifecycle.Event.ON_STOP;
            case ON_STOP:
            case ON_DESTROY:
            default:
              throw new LifecycleEndedException();
          }
        }
      };

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android LifecycleOwners.
   *
   * @param owner the owner to scope for
   * @return a {@link AndroidLifecycleScopeProvider} against this owner.
   */
  public static AndroidLifecycleScopeProvider from(LifecycleOwner owner) {
    return from(owner.getLifecycle());
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param lifecycle the lifecycle to scope for
   * @return a {@link AndroidLifecycleScopeProvider} against this lifecycle.
   */
  public static AndroidLifecycleScopeProvider from(Lifecycle lifecycle) {
    return new AndroidLifecycleScopeProvider(lifecycle);
  }

  private final LifecycleEventsObservable lifecycleObservable;

  private AndroidLifecycleScopeProvider(Lifecycle lifecycle) {
    this.lifecycleObservable = new LifecycleEventsObservable(lifecycle);
  }

  @Override public Observable<Lifecycle.Event> lifecycle() {
    return lifecycleObservable;
  }

  @Override public Function<Lifecycle.Event, Lifecycle.Event> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Override public Lifecycle.Event peekLifecycle() {
    return lifecycleObservable.getValue();
  }
}
