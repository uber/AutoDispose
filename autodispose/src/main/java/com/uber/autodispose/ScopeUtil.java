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

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.concurrent.Callable;

/**
 * Utilities for dealing with scopes.
 */
public final class ScopeUtil {

  private static final Function<Object, LifecycleEndEvent> TRANSFORM_TO_END =
      new Function<Object, LifecycleEndEvent>() {
        @Override public LifecycleEndEvent apply(Object o) throws Exception {
          return LifecycleEndEvent.INSTANCE;
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

  public static <E> Maybe<LifecycleEndEvent> deferredResolvedLifecycle(
      LifecycleScopeProvider<E> provider) {
    return deferredResolvedLifecycle(provider, true, true);
  }

  public static <E> Maybe<LifecycleEndEvent> deferredResolvedLifecycle(
      final LifecycleScopeProvider<E> provider, final boolean checkStartBoundary,
      final boolean checkEndBoundary) {
    return Maybe.defer(new Callable<MaybeSource<? extends LifecycleEndEvent>>() {
      @Override public MaybeSource<? extends LifecycleEndEvent> call() throws Exception {
        E lastEvent = provider.peekLifecycle();
        if (checkStartBoundary && lastEvent == null) {
          throw new LifecycleNotStartedException();
        }
        E endEvent;
        try {
          endEvent = provider.correspondingEvents()
              .apply(lastEvent);
        } catch (Exception e) {
          if (checkEndBoundary && e instanceof LifecycleEndedException) {
            throw e;
          } else {
            return Maybe.error(e);
          }
        }
        return resolveLifecycleMaybe(provider.lifecycle(), endEvent);
      }
    });
  }

  public static <E> Maybe<LifecycleEndEvent> resolveLifecycleMaybe(Observable<E> lifecycle,
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
  public enum LifecycleEndEvent {
    INSTANCE
  }
}
