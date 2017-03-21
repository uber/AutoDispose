/*
 * Copyright (C) 2017. Uber Technologies
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

import java.util.concurrent.Callable;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Utilities for dealing with scopes, usually for providers. This includes factories for resolving
 * {@link Maybe} representations of scopes, corresponding events, etc.
 */
public final class ScopeUtil {

  private static final Function<Object, LifecycleEndNotification> TRANSFORM_TO_END =
      new Function<Object, LifecycleEndNotification>() {
        @Override public LifecycleEndNotification apply(Object o) throws Exception {
          return LifecycleEndNotification.INSTANCE;
        }
      };

  private static final Predicate<Boolean> IDENTITY_BOOLEAN_PREDICATE = new Predicate<Boolean>() {
    @Override public boolean test(Boolean b) throws Exception {
      return b;
    }
  };

  private ScopeUtil() {
    throw new InstantiationError();
  }

  /**
   * Overload for resolving lifecycle providers that defaults to checking start and end boundaries
   * of lifecycles. That is, they will ensure that the lifecycle has both started and not ended.
   *
   * @param provider the {@link LifecycleScopeProvider} to resolve.
   * @param <E> the lifecycle event type.
   * @return a resolved {@link Maybe} representation of a given provider
   */
  public static <E> Maybe<LifecycleEndNotification> deferredResolvedLifecycle(
      LifecycleScopeProvider<E> provider) {
    return deferredResolvedLifecycle(provider, true, true);
  }

  /**
   * @param provider the {@link LifecycleScopeProvider} to resolve.
   * @param checkStartBoundary whether or not to check that the lifecycle has started
   * @param checkEndBoundary whether or not to check that the lifecycle has ended
   * @param <E> the lifecycle event type
   * @return a resolved {@link Maybe} representation of a given provider
   */
  public static <E> Maybe<LifecycleEndNotification> deferredResolvedLifecycle(
      final LifecycleScopeProvider<E> provider,
      final boolean checkStartBoundary,
      final boolean checkEndBoundary) {
    return Maybe.defer(new Callable<MaybeSource<? extends LifecycleEndNotification>>() {
      @Override public MaybeSource<? extends LifecycleEndNotification> call() throws Exception {
        E lastEvent = provider.peekLifecycle();
        if (checkStartBoundary && lastEvent == null) {
          LifecycleNotStartedException exception = new LifecycleNotStartedException();
          if (AutoDisposePlugins.outsideLifecycleHandler != null) {
            AutoDisposePlugins.outsideLifecycleHandler.accept(exception);
          } else {
            throw exception;
          }
        }
        E endEvent;
        try {
          endEvent = provider.correspondingEvents()
              .apply(lastEvent);
        } catch (Exception e) {
          if (checkEndBoundary && e instanceof LifecycleEndedException) {
            if (AutoDisposePlugins.outsideLifecycleHandler != null) {
              AutoDisposePlugins.outsideLifecycleHandler.accept((LifecycleEndedException) e);
              return Maybe.empty();
            } else {
              throw e;
            }
          } else {
            return Maybe.error(e);
          }
        }
        return resolveScopeFromLifecycle(provider.lifecycle(), endEvent);
      }
    });
  }

  /**
   * @param lifecycle the stream of lifecycle events
   * @param endEvent the target end event
   * @param <E> the lifecycle event type
   * @return a resolved {@link Maybe} representation of a given lifecycle, targeting the given event
   */
  public static <E> Maybe<LifecycleEndNotification> resolveScopeFromLifecycle(
      Observable<E> lifecycle,
      final E endEvent) {
    return lifecycle.skip(1)
        .map(new Function<E, Boolean>() {
          @Override public Boolean apply(E e) throws Exception {
            return e.equals(endEvent);
          }
        })
        .filter(IDENTITY_BOOLEAN_PREDICATE)
        .map(TRANSFORM_TO_END)
        .firstElement();
  }

  /**
   * A simple instance enum used to signify that the end of a lifecycle has occurred. This should
   * be treated solely as a notification and does not have any real value.
   */
  public enum LifecycleEndNotification {
    INSTANCE
  }
}
