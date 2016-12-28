package com.uber.autodispose.clause.scope;

import com.uber.autodispose.LifecycleScopeProvider;
import com.uber.autodispose.ScopeProvider;
import io.reactivex.Maybe;

/**
 * Scope clause for the scope provisioning steps.
 */
public interface ScopeClause<T> {

  T withScope(ScopeProvider provider);

  T withScope(LifecycleScopeProvider<?> provider);

  T withScope(Maybe<?> lifecycle);
}
