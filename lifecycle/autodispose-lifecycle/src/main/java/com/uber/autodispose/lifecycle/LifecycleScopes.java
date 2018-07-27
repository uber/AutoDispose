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

import com.uber.autodispose.AutoDisposePlugins;
import com.uber.autodispose.OutsideScopeException;
import com.uber.autodispose.internal.ScopeEndNotification;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.Comparator;
import java.util.concurrent.Callable;

/**
 * Utilities for dealing with {@link LifecycleScopeProvider}s. This includes factories for resolving
 * {@link Maybe} representations of scopes, corresponding events, etc.
 */
public final class LifecycleScopes {

  private static final Function<Object, ScopeEndNotification> TRANSFORM_TO_END =
      new Function<Object, ScopeEndNotification>() {
        @Override public ScopeEndNotification apply(Object o) {
          return ScopeEndNotification.INSTANCE;
        }
      };

  private static final Predicate<Boolean> IDENTITY_BOOLEAN_PREDICATE = new Predicate<Boolean>() {
    @Override public boolean test(Boolean b) {
      return b;
    }
  };

  private static final Comparator<Comparable<Object>> COMPARABLE_COMPARATOR =
      new Comparator<Comparable<Object>>() {
        @Override public int compare(Comparable<Object> o1, Comparable<Object> o2) {
          return o1.compareTo(o2);
        }
      };

  private LifecycleScopes() {
    throw new InstantiationError();
  }

  /**
   * Overload for resolving lifecycle providers that defaults to checking start and end boundaries
   * of lifecycles. That is, they will ensure that the lifecycle has both started and not ended.
   *
   * <p><em>Note:</em> This resolves the scope immediately, so consider deferring execution as
   * needed, such as using {@link Maybe#defer(Callable) defer}.
   *
   * @param provider the {@link LifecycleScopeProvider} to resolve.
   * @param <E> the lifecycle event type
   * @return a resolved {@link Maybe} representation of a given provider
   * @throws OutsideScopeException if the {@link LifecycleScopeProvider#correspondingEvents()}
   * throws an {@link OutsideScopeException} during resolution.
   */
  public static <E> Maybe<?> resolveScopeFromLifecycle(final LifecycleScopeProvider<E> provider)
      throws OutsideScopeException {
    return resolveScopeFromLifecycle(provider, true);
  }

  /**
   * Overload for resolving lifecycle providers allows configuration of checking "end" boundaries
   * of lifecycles. That is, they will ensure that the lifecycle has both started and not ended,
   * and otherwise will throw one of {@link LifecycleNotStartedException} (if {@link
   * LifecycleScopeProvider#peekLifecycle() peekLifecycle} returns {@code null}) or
   * {@link LifecycleEndedException} if the lifecycle is ended. To configure the runtime behavior
   * of these exceptions, see {@link AutoDisposePlugins}.
   *
   * <p><em>Note:</em> This resolves the scope immediately, so consider deferring execution as
   * needed, such as using {@link Maybe#defer(Callable) defer}.
   *
   * @param provider the {@link LifecycleScopeProvider} to resolve.
   * @param checkEndBoundary whether or not to check that the lifecycle has ended
   * @param <E> the lifecycle event type
   * @return a resolved {@link Maybe} representation of a given provider
   * @throws OutsideScopeException if the {@link LifecycleScopeProvider#correspondingEvents()}
   * throws an {@link OutsideScopeException} during resolution.
   */
  public static <E> Maybe<?> resolveScopeFromLifecycle(final LifecycleScopeProvider<E> provider,
      final boolean checkEndBoundary) throws OutsideScopeException {
    E lastEvent = provider.peekLifecycle();
    CorrespondingEventsFunction<E> eventsFunction = provider.correspondingEvents();
    if (lastEvent == null) {
      throw new LifecycleNotStartedException();
    }
    E endEvent;
    try {
      endEvent = eventsFunction.apply(lastEvent);
    } catch (Exception e) {
      if (checkEndBoundary && e instanceof LifecycleEndedException) {
        Consumer<? super OutsideScopeException> handler =
            AutoDisposePlugins.getOutsideScopeHandler();
        if (handler != null) {
          try {
            handler.accept((LifecycleEndedException) e);

            // Swallowed the end exception, just silently dispose immediately.
            return Maybe.just(ScopeEndNotification.INSTANCE);
          } catch (Exception e1) {
            return Maybe.error(e1);
          }
        }
        throw e;
      }
      return Maybe.error(e);
    }
    return resolveScopeFromLifecycle(provider.lifecycle(), endEvent);
  }

  /**
   * @param lifecycle the stream of lifecycle events
   * @param endEvent the target end event
   * @param <E> the lifecycle event type
   * @return a resolved {@link Maybe} representation of a given lifecycle, targeting the given event
   */
  public static <E> Maybe<?> resolveScopeFromLifecycle(Observable<E> lifecycle, final E endEvent) {
    @Nullable Comparator<E> comparator = null;
    if (endEvent instanceof Comparable) {
      //noinspection unchecked
      comparator = (Comparator<E>) COMPARABLE_COMPARATOR;
    }
    return resolveScopeFromLifecycle(lifecycle, endEvent, comparator);
  }

  /**
   * @param lifecycle the stream of lifecycle events
   * @param endEvent the target end event
   * @param comparator an optional comparator for checking event equality.
   * @param <E> the lifecycle event type
   * @return a resolved {@link Maybe} representation of a given lifecycle, targeting the given event
   */
  public static <E> Maybe<?> resolveScopeFromLifecycle(Observable<E> lifecycle, final E endEvent,
      @Nullable final Comparator<E> comparator) {
    Function<E, Boolean> equalityFunction;
    if (comparator != null) {
      equalityFunction = new Function<E, Boolean>() {
        @Override public Boolean apply(E e) {
          return comparator.compare(e, endEvent) >= 0;
        }
      };
    } else {
      equalityFunction = new Function<E, Boolean>() {
        @Override public Boolean apply(E e) {
          return e.equals(endEvent);
        }
      };
    }
    return lifecycle.skip(1)
        .map(equalityFunction)
        .filter(IDENTITY_BOOLEAN_PREDICATE)
        .map(TRANSFORM_TO_END)
        .firstElement();
  }
}
