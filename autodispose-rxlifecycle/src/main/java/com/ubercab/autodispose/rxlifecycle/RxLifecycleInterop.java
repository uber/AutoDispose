package com.ubercab.autodispose.rxlifecycle;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.uber.autodispose.AutoDispose.ScopeHandler;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.ScopeProvider;
import io.reactivex.Maybe;

/**
 * Interop for RxLifecycle. It provides static utility methods to convert {@link
 * LifecycleProvider} to {@link ScopeProvider}.
 *
 * <p>There are several static utility converter
 * methods such as {@link #from(LifecycleProvider)} for {@link
 * LifecycleProvider#bindToLifecycle()} and {@link #from(LifecycleProvider, Object)} for
 * {@link LifecycleProvider#bindUntilEvent(Object)}.
 * <p>
 *
 * <em>Note:</em> RxLifecycle treats the {@link OutsideLifecycleException}
 * as normal terminal event. There is no mapping to {@link LifecycleEndedException} and in such
 * cases the stream is normally disposed.
 */
public final class RxLifecycleInterop {

  private RxLifecycleInterop() {
    throw new AssertionError("No Instances");
  }

  private static final Object DEFAULT_THROWAWAY_OBJECT = new Object();

  /**
   * Converter for transforming {@link LifecycleProvider} to {@link ScopeProvider}.
   * It disposes the source when the next reasonable event occurs.
   * <p>
   * Example usage:
   * <pre><code>
   *   Observable.just(1)
   *        .to(RxLifecycleInterop.from(lifecycleProvider))
   *        .subscribe(...)
   * </code></pre>
   *
   * @param <E> the lifecycle event.
   * @param provider the {@link LifecycleProvider} for RxLifecycle.
   * @return a {@link ScopeHandler} to create AutoDisposing transformation
   */
  public static <E> ScopeProvider from(final LifecycleProvider<E> provider) {
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

  /**
   * Converter for transforming {@link LifecycleProvider} to {@link ScopeProvider}.
   * It disposes the source when a specific event occurs.
   * <p>
   * Example usage:
   * <pre><code>
   *   Observable.just(1)
   *        .to(RxLifecycleInterop.from(lifecycleProvider, event))
   *        .subscribe(...)
   * </code></pre>
   *
   * @param <E> the lifecycle event.
   * @param provider the {@link LifecycleProvider} for RxLifecycle.
   * @param event the event at which the source is disposed.
   * @return a {@link ScopeHandler} to create AutoDisposing transformation
   */
  public static <E> ScopeProvider from(final LifecycleProvider<E> provider, final E event) {
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
