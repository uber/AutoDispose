package com.uber.autodispose;

import io.reactivex.Observable;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.functions.Function;
import javax.annotation.Nullable;

/**
 * An interface that, when implemented, provides information to AutoDispose to allow it to resolve
 * the next lifecycle event and construct a Maybe representation of it from hte {@link #lifecycle()}
 * stream.
 *
 * @param <E> the the lifecycle event type.
 */
public interface LifecycleScopeProvider<E> {

  /**
   * @return a sequence of lifecycle events.
   */
  @CheckReturnValue Observable<E> lifecycle();

  /**
   * @return a sequence of lifecycle events. It's recommended to back this with a static instance to
   * avoid unnecessary object allocationn.
   */
  @CheckReturnValue Function<E, E> correspondingEvents();

  /**
   * @return the last seen lifecycle event, or {@code null} if none.
   */
  @Nullable E peekLifecycle();
}
