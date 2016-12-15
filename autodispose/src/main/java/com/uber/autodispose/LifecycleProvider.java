package com.uber.autodispose;

import io.reactivex.Observable;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.functions.Function;
import javax.annotation.Nullable;

/**
 * @param <E> the the lifecycle event type.
 */
public interface LifecycleProvider<E> {

  /**
   * @return a sequence of lifecycle events.
   */
  @CheckReturnValue
  Observable<E> lifecycle();

  /**
   * @return a sequence of lifecycle events.
   */
  @CheckReturnValue
  Function<E, E> correspondingEvents();

  /**
   * @return the last seen lifecycle event, or {@code null} if none.
   */
  @Nullable
  E peekLifecycle();
}
