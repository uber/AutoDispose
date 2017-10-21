package com.ubercab.autodispose.rxlifecycle;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.ScopeProvider;
import io.reactivex.Maybe;

/**
 * Interop class for RxLifecycle. It provides static utility methods to convert {@link
 * LifecycleProvider} to {@link ScopeProvider}.
 *
 * <p>There are several static utility converter
 * methods such as {@link #fromBindLifecycle(LifecycleProvider)} for {@link
 * LifecycleProvider#bindToLifecycle()} and {@link #fromBindUntilEvent(LifecycleProvider, Object)} for
 * {@link LifecycleProvider#bindUntilEvent(Object)}.
 * <p>
 *
 * <em>Note:</em> RxLifecycle treats the {@link OutsideLifecycleException}
 * as normal terminal event. There is no mapping to {@link LifecycleEndedException} and in such
 * cases the stream is normally disposed.
 */
public final class RXLifecycleInterop {

  private RXLifecycleInterop() {
    throw new AssertionError("No Instances");
  }

  private static final Object DEFAULT_THROWAWAY_OBJECT = new Object();

  public static <E> ScopeProvider fromBindLifecycle(final LifecycleProvider<E> provider) {
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

  public static <E> ScopeProvider fromBindUntilEvent(final LifecycleProvider<E> provider, final E event) {
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
