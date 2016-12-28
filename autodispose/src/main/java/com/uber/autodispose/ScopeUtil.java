package com.uber.autodispose;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Utilities for dealing with scopes.
 */
public final class ScopeUtil {

  private static final Function<Object, LifecycleEndEvent> TRANSFORM_TO_END =
      o -> LifecycleEndEvent.INSTANCE;

  private ScopeUtil() {
    throw new InstantiationError();
  }

  public static <E> Maybe<LifecycleEndEvent> deferredResolvedLifecycle(
      LifecycleScopeProvider<E> provider) {
    return deferredResolvedLifecycle(provider, true, true);
  }

  public static <E> Maybe<LifecycleEndEvent> deferredResolvedLifecycle(
      LifecycleScopeProvider<E> provider,
      boolean checkStartBoundary,
      boolean checkEndBoundary) {
    return Maybe.defer(() -> {
      E lastEvent = provider.peekLifecycle();
      if (checkStartBoundary && lastEvent == null) {
        throw new LifecycleNotStartedException();
      }
      E endEvent;
      try {
        endEvent = provider.correspondingEvents().apply(lastEvent);
      } catch (Exception e) {
        if (checkEndBoundary && e instanceof LifecycleEndedException) {
          throw e;
        } else {
          return Maybe.error(e);
        }
      }
      return resolveLifecycleMaybe(provider.lifecycle(), endEvent);
    });
  }

  public static <E> Maybe<LifecycleEndEvent> resolveLifecycleMaybe(Observable<E> lifecycle,
      E endEvent) {
    return lifecycle.skip(1)
        .map(e -> e.equals(endEvent))
        .filter(b -> b)
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
