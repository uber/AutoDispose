package com.ubercab.autodispose.rxlifecycleinterop;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.trello.rxlifecycle2.RxLifecycle;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.ScopeProvider;
import io.reactivex.Maybe;

/**
 * Interop class for {@link RxLifecycle}. It provides static utility methods to convert {@link
 * LifecycleProvider} to {@link ScopeProvider}
 * <p> There are several static utility converter
 * methods such as {@link #bindLifecycle(LifecycleProvider)} for {@link
 * LifecycleProvider#bindToLifecycle()} and {@link #bindUntilEvent(LifecycleProvider, Object)} for
 * {@link LifecycleProvider#bindUntilEvent(Object)} </p>
 *
 * <em>Unlike {@link AutoDispose}, {@link RxLifecycle} treats the {@link OutsideLifecycleException}
 * as normal terminal event. There is no mapping to {@link LifecycleEndedException} and in such
 * cases the stream is normally disposed </em>
 */
public final class RXLifeCycleInterop {

  private RXLifeCycleInterop() {
    throw new AssertionError("No Instances");
  }

  private static final Object DEFAULT_THROWAWAY_OBJECT = new Object();

  static <E> ScopeProvider bindLifecycle(final LifecycleProvider<E> provider) {
    return new ScopeProvider() {
      @Override public Maybe<?> requestScope() {
        return provider.lifecycle()
            .compose(provider.bindToLifecycle())
            .ignoreElements()
            .toMaybe()
            .defaultIfEmpty(DEFAULT_THROWAWAY_OBJECT);
      }
    };
  }

  static <E> ScopeProvider bindUntilEvent(final LifecycleProvider<E> provider, final E event) {
    return new ScopeProvider() {
      @Override public Maybe<?> requestScope() {
        return provider.lifecycle()
            .compose(provider.bindUntilEvent(event))
            .ignoreElements()
            .toMaybe()
            .defaultIfEmpty(DEFAULT_THROWAWAY_OBJECT);
      }
    };
  }
}
