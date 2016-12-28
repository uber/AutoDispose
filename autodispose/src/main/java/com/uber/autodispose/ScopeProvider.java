package com.uber.autodispose;

import io.reactivex.Maybe;
import io.reactivex.annotations.CheckReturnValue;

/**
 * Proves a {@link Maybe} representation of a scope. The emission of this is the signal
 */
public interface ScopeProvider {

  /**
   * @return a Maybe that, upon emission, will trigger disposal.
   */
  @CheckReturnValue Maybe<?> requestScope();
}
