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

package com.uber.autodispose.android.lifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import com.uber.autodispose.OutsideScopeException;
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction;
import com.uber.autodispose.lifecycle.LifecycleEndedException;
import com.uber.autodispose.lifecycle.LifecycleScopeProvider;
import com.uber.autodispose.lifecycle.LifecycleScopes;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;

/**
 * A {@link LifecycleScopeProvider} that can provide scoping for Android {@link Lifecycle} and
 * {@link LifecycleOwner} classes.
 * <p>
 * <pre><code>
 *   AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner))
 * </code></pre>
 */
public final class AndroidLifecycleScopeProvider implements LifecycleScopeProvider<Lifecycle.Event> {

  private static final CorrespondingEventsFunction<Lifecycle.Event> DEFAULT_CORRESPONDING_EVENTS =
      lastEvent -> {
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
            throw new LifecycleEndedException("Lifecycle has ended! Last event was " + lastEvent);
        }
      };

  private final CorrespondingEventsFunction<Lifecycle.Event> boundaryResolver;

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android LifecycleOwners.
   *
   * @param owner the owner to scope for.
   * @return a {@link AndroidLifecycleScopeProvider} against this owner.
   */
  public static AndroidLifecycleScopeProvider from(LifecycleOwner owner) {
    return from(owner.getLifecycle());
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android LifecycleOwners.
   *
   * @param owner the owner to scope for.
   * @param untilEvent the event until the scope is valid.
   * @return a {@link AndroidLifecycleScopeProvider} against this owner.
   */
  public static AndroidLifecycleScopeProvider from(LifecycleOwner owner, Lifecycle.Event untilEvent) {
    return from(owner.getLifecycle(), untilEvent);
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param lifecycle the lifecycle to scope for.
   * @return a {@link AndroidLifecycleScopeProvider} against this lifecycle.
   */
  public static AndroidLifecycleScopeProvider from(Lifecycle lifecycle) {
    return from(lifecycle, DEFAULT_CORRESPONDING_EVENTS);
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param lifecycle the lifecycle to scope for.
   * @param untilEvent the event until the scope is valid.
   * @return a {@link AndroidLifecycleScopeProvider} against this lifecycle.
   */
  public static AndroidLifecycleScopeProvider from(Lifecycle lifecycle, Lifecycle.Event untilEvent) {
    return from(lifecycle, new UntilEventFunction(untilEvent));
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param owner the owner to scope for.
   * @param boundaryResolver function that resolves the event boundary.
   * @return a {@link AndroidLifecycleScopeProvider} against this owner.
   */
  public static AndroidLifecycleScopeProvider from(LifecycleOwner owner,
      CorrespondingEventsFunction<Lifecycle.Event> boundaryResolver) {
    return from(owner.getLifecycle(), boundaryResolver);
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param lifecycle the lifecycle to scope for.
   * @param boundaryResolver function that resolves the event boundary.
   * @return a {@link AndroidLifecycleScopeProvider} against this lifecycle.
   */
  public static AndroidLifecycleScopeProvider from(Lifecycle lifecycle,
      CorrespondingEventsFunction<Lifecycle.Event> boundaryResolver) {
    return new AndroidLifecycleScopeProvider(lifecycle, boundaryResolver);
  }

  private final LifecycleEventsObservable lifecycleObservable;

  private AndroidLifecycleScopeProvider(Lifecycle lifecycle,
      CorrespondingEventsFunction<Lifecycle.Event> boundaryResolver) {
    this.lifecycleObservable = new LifecycleEventsObservable(lifecycle);
    this.boundaryResolver = boundaryResolver;
  }

  @Override public Observable<Lifecycle.Event> lifecycle() {
    return lifecycleObservable;
  }

  @Override public CorrespondingEventsFunction<Lifecycle.Event> correspondingEvents() {
    return boundaryResolver;
  }

  @Override public Lifecycle.Event peekLifecycle() {
    lifecycleObservable.backfillEvents();
    return lifecycleObservable.getValue();
  }

  @Override public CompletableSource requestScope() {
    return LifecycleScopes.resolveScopeFromLifecycle(this);
  }

  private static class UntilEventFunction implements CorrespondingEventsFunction<Lifecycle.Event> {
    private final Lifecycle.Event untilEvent;

    UntilEventFunction(Lifecycle.Event untilEvent) {
      this.untilEvent = untilEvent;
    }

    @Override public Lifecycle.Event apply(Lifecycle.Event event) throws OutsideScopeException {
      return untilEvent;
    }
  }
}
