package com.uber.autodispose;

import io.reactivex.Maybe;
import io.reactivex.annotations.Experimental;

/**
 * Static factories for creating new scopers.
 */
@Experimental public final class AutoDispose {

  private AutoDispose() {
    // No instances.
  }

  public static <T> ObservableScoper<T> observableScoper(Maybe<?> scope) {
    return new ObservableScoper<>(scope);
  }

  public static <T> ObservableScoper<T> observableScoper(LifecycleScopeProvider<?> provider) {
    return new ObservableScoper<>(provider);
  }

  public static <T> ObservableScoper<T> observableScoper(ScopeProvider provider) {
    return new ObservableScoper<>(provider);
  }

  public static <T> FlowableScoper<T> flowableScoper(Maybe<?> scope) {
    return new FlowableScoper<>(scope);
  }

  public static <T> FlowableScoper<T> flowableScoper(LifecycleScopeProvider<?> provider) {
    return new FlowableScoper<>(provider);
  }

  public static <T> FlowableScoper<T> flowableScoper(ScopeProvider provider) {
    return new FlowableScoper<>(provider);
  }

  public static <T> MaybeScoper<T> maybeScoper(Maybe<?> scope) {
    return new MaybeScoper<>(scope);
  }

  public static <T> MaybeScoper<T> maybeScoper(LifecycleScopeProvider<?> provider) {
    return new MaybeScoper<>(provider);
  }

  public static <T> MaybeScoper<T> maybeScoper(ScopeProvider provider) {
    return new MaybeScoper<>(provider);
  }

  public static <T> SingleScoper<T> singleScoper(Maybe<?> scope) {
    return new SingleScoper<>(scope);
  }

  public static <T> SingleScoper<T> singleScoper(LifecycleScopeProvider<?> provider) {
    return new SingleScoper<>(provider);
  }

  public static <T> SingleScoper<T> singleScoper(ScopeProvider provider) {
    return new SingleScoper<>(provider);
  }

  public static CompletableScoper completableScoper(Maybe<?> scope) {
    return new CompletableScoper(scope);
  }

  public static CompletableScoper completableScoper(LifecycleScopeProvider<?> provider) {
    return new CompletableScoper(provider);
  }

  public static CompletableScoper completableScoper(ScopeProvider provider) {
    return new CompletableScoper(provider);
  }
}
