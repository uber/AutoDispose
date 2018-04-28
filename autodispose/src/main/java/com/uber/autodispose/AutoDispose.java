/*
 * Copyright (c) 2017. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uber.autodispose;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.ObservableConverter;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.parallel.ParallelFlowable;
import io.reactivex.subscribers.TestSubscriber;
import java.util.concurrent.Callable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static com.uber.autodispose.AutoDisposeUtil.checkNotNull;
import static com.uber.autodispose.ScopeUtil.deferredResolvedLifecycle;

/**
 * Factories for autodispose converters that can be used with RxJava types' corresponding
 * {@code as(...)} methods to transform them into auto-disposing streams.
 * <p>
 * There are several static {@code autoDisposable(...)} entry points, with the most basic being a
 * simple {@link #autoDisposable(Maybe)}. The provided {@link Maybe} is ultimately what every scope
 * resolves to under the hood, and AutoDispose has some built-in understanding for predefined types.
 * The scope is considered ended upon onSuccess emission of this {@link Maybe}.
 * <p>
 * This is structured in such a way to be friendly to autocompletion in IDEs, where the no-parameter
 * generic method will autocomplete with the appropriate generic parameters in Java <7, or
 * implicitly in >=8.
 *
 * @see Flowable#as(io.reactivex.FlowableConverter)
 * @see Observable#as(io.reactivex.ObservableConverter)
 * @see Maybe#as(io.reactivex.MaybeConverter)
 * @see Single#as(io.reactivex.SingleConverter)
 * @see Completable#as(io.reactivex.CompletableConverter)
 */
@SuppressWarnings("deprecation") // Temporary until we remove and inline the Scoper classes
public final class AutoDispose {

  /**
   * The intermediary return type of the {@code with(...)} factories in {@link AutoDispose}. See the
   * documentation on {@link AutoDispose} for more information on why this interface exists.
   */
  @Deprecated public interface ScopeHandler {
    /**
     * Entry point for auto-disposing {@link Flowable}s.
     * <p>
     * Example usage:
     * <pre><code>
     *   Flowable.just(1)
     *        .to(AutoDispose.with(scope).<Integer>forFlowable())
     *        .subscribe(...)
     * </code></pre>
     *
     * @param <T> the stream type.
     * @return a {@link Function} to transform with {@link Flowable#to(Function)}
     */
    @CheckReturnValue <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> forFlowable();

    /**
     * Entry point for auto-disposing {@link Observable}s.
     * <p>
     * Example usage:
     * <pre><code>
     *   Observable.just(1)
     *        .to(AutoDispose.with(scope).<Integer>forObservable())
     *        .subscribe(...)
     * </code></pre>
     *
     * @param <T> the stream type.
     * @return a {@link Function} to transform with {@link Observable#to(Function)}
     */
    @CheckReturnValue
    <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> forObservable();

    /**
     * Entry point for auto-disposing {@link Maybe}s.
     * <p>
     * Example usage:
     * <pre><code>
     *   Maybe.just(1)
     *        .to(AutoDispose.with(scope).<Integer>forMaybe())
     *        .subscribe(...)
     * </code></pre>
     *
     * @param <T> the stream type.
     * @return a {@link Function} to transform with {@link Maybe#to(Function)}
     */
    @CheckReturnValue <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> forMaybe();

    /**
     * Entry point for auto-disposing {@link Single}s.
     * <p>
     * Example usage:
     * <pre><code>
     *   Single.just(1)
     *        .to(AutoDispose.with(scope).<Integer>forSingle())
     *        .subscribe(...)
     * </code></pre>
     *
     * @param <T> the stream type.
     * @return a {@link Function} to transform with {@link Single#to(Function)}
     */
    @CheckReturnValue <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> forSingle();

    /**
     * Entry point for auto-disposing {@link Completable}s.
     * <p>
     * Example usage:
     * <pre><code>
     *   Completable.complete()
     *        .to(AutoDispose.with(scope).forCompletable())
     *        .subscribe(...)
     * </code></pre>
     *
     * @return a {@link Function} to transform with {@link Completable#to(Function)}
     */
    @CheckReturnValue Function<Completable, CompletableSubscribeProxy> forCompletable();
  }

  /**
   * The factory for {@link Maybe} scopes.
   *
   * @param scope the target scope
   * @return a {@link ScopeHandler} for this scope to create AutoDisposing transformation
   * {@link Function}s
   * @deprecated This will be removed in AutoDispose 1.0. Please use the {@code autoDisposable()}
   *             APIs.
   */
  @Deprecated @CheckReturnValue public static ScopeHandler with(Maybe<?> scope) {
    return new MaybeScopeHandlerImpl(scope);
  }

  /**
   * The factory for {@link ScopeProvider} scopes.
   *
   * @param scope the target scope
   * @return a {@link ScopeHandler} for this scope to create AutoDisposing transformation
   * {@link Function}s
   * @deprecated This will be removed in AutoDispose 1.0. Please use the {@code autoDisposable()}
   *             APIs.
   */
  @Deprecated @CheckReturnValue public static ScopeHandler with(ScopeProvider scope) {
    return new ScopeProviderHandlerImpl(scope);
  }

  /**
   * The factory for {@link LifecycleScopeProvider} scopes.
   *
   * @param scope the target scope
   * @return a {@link ScopeHandler} for this scope to create AutoDisposing transformation
   * {@link Function}s
   * @deprecated This will be removed in AutoDispose 1.0. Please use the {@code autoDisposable()}
   *             APIs.
   */
  @Deprecated @CheckReturnValue public static ScopeHandler with(LifecycleScopeProvider<?> scope) {
    return new LifecycleScopeProviderHandlerImpl(scope);
  }

  private static final class MaybeScopeHandlerImpl implements ScopeHandler {

    final Maybe<?> scope;

    MaybeScopeHandlerImpl(Maybe<?> scope) {
      this.scope = scope;
    }

    @Override public <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> forFlowable() {
      return new FlowableScoper<>(scope);
    }

    @Override
    public <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> forObservable() {
      return new ObservableScoper<>(scope);
    }

    @Override public <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> forMaybe() {
      return new MaybeScoper<>(scope);
    }

    @Override public <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> forSingle() {
      return new SingleScoper<>(scope);
    }

    @Override public Function<Completable, CompletableSubscribeProxy> forCompletable() {
      return new CompletableScoper(scope);
    }
  }

  private static final class ScopeProviderHandlerImpl implements ScopeHandler {

    final ScopeProvider scope;

    ScopeProviderHandlerImpl(ScopeProvider scope) {
      this.scope = scope;
    }

    @Override public <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> forFlowable() {
      return new FlowableScoper<>(scope);
    }

    @Override
    public <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> forObservable() {
      return new ObservableScoper<>(scope);
    }

    @Override public <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> forMaybe() {
      return new MaybeScoper<>(scope);
    }

    @Override public <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> forSingle() {
      return new SingleScoper<>(scope);
    }

    @Override public Function<Completable, CompletableSubscribeProxy> forCompletable() {
      return new CompletableScoper(scope);
    }
  }

  private static final class LifecycleScopeProviderHandlerImpl implements ScopeHandler {

    final LifecycleScopeProvider<?> scope;

    LifecycleScopeProviderHandlerImpl(LifecycleScopeProvider<?> scope) {
      this.scope = scope;
    }

    @Override public <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> forFlowable() {
      return new FlowableScoper<>(scope);
    }

    @Override
    public <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> forObservable() {
      return new ObservableScoper<>(scope);
    }

    @Override public <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> forMaybe() {
      return new MaybeScoper<>(scope);
    }

    @Override public <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> forSingle() {
      return new SingleScoper<>(scope);
    }

    @Override public Function<Completable, CompletableSubscribeProxy> forCompletable() {
      return new CompletableScoper(scope);
    }
  }

  /**
   * Entry point for auto-disposing streams from a {@link ScopeProvider}.
   * <p>
   * Example usage:
   * <pre><code>
   *   Observable.just(1)
   *        .as(AutoDispose.<Integer>autoDisposable(scope))
   *        .subscribe(...)
   * </code></pre>
   *
   * @param provider the target scope provider
   * @param <T> the stream type.
   * @return an {@link AutoDisposeConverter} to transform with operators like
   * {@link Observable#as(ObservableConverter)}
   */
  public static <T> AutoDisposeConverter<T> autoDisposable(final ScopeProvider provider) {
    checkNotNull(provider, "provider == null");
    return autoDisposable(Maybe.defer(new Callable<MaybeSource<?>>() {
      @Override public MaybeSource<?> call() {
        return provider.requestScope();
      }
    }));
  }

  /**
   * Entry point for auto-disposing streams from a {@link LifecycleScopeProvider}.
   * <p>
   * Example usage:
   * <pre><code>
   *   Observable.just(1)
   *        .as(AutoDispose.<Integer>autoDisposable(scope))
   *        .subscribe(...)
   * </code></pre>
   *
   * @param provider the target lifecycle scope provider
   * @param <T> the stream type.
   * @return an {@link AutoDisposeConverter} to transform with operators like
   * {@link Observable#as(ObservableConverter)}
   */
  public static <T> AutoDisposeConverter<T> autoDisposable(
      final LifecycleScopeProvider<?> provider) {
    return autoDisposable(deferredResolvedLifecycle(checkNotNull(provider, "provider == null")));
  }

  /**
   * Entry point for auto-disposing streams from a {@link Maybe}.
   * <p>
   * Example usage:
   * <pre><code>
   *   Observable.just(1)
   *        .as(AutoDispose.<Integer>autoDisposable(scope))
   *        .subscribe(...)
   * </code></pre>
   *
   * @param scope the target scope
   * @param <T> the stream type.
   * @return an {@link AutoDisposeConverter} to transform with operators like
   * {@link Observable#as(ObservableConverter)}
   */
  public static <T> AutoDisposeConverter<T> autoDisposable(final Maybe<?> scope) {
    checkNotNull(scope, "scope == null");
    return new AutoDisposeConverter<T>() {
      @Override public ParallelFlowableSubscribeProxy<T> apply(final ParallelFlowable<T> upstream) {
        return new ParallelFlowableSubscribeProxy<T>() {
          @Override public void subscribe(Subscriber<? super T>[] subscribers) {
            new AutoDisposeParallelFlowable<>(upstream, scope).subscribe(subscribers);
          }
        };
      }

      @Override public CompletableSubscribeProxy apply(final Completable upstream) {
        return new CompletableSubscribeProxy() {
          @Override public Disposable subscribe() {
            return new AutoDisposeCompletable(upstream, scope).subscribe();
          }

          @Override public Disposable subscribe(Action action) {
            return new AutoDisposeCompletable(upstream, scope).subscribe(action);
          }

          @Override
          public Disposable subscribe(Action action, Consumer<? super Throwable> onError) {
            return new AutoDisposeCompletable(upstream, scope).subscribe(action, onError);
          }

          @Override public void subscribe(CompletableObserver observer) {
            new AutoDisposeCompletable(upstream, scope).subscribe(observer);
          }

          @Override public <E extends CompletableObserver> E subscribeWith(E observer) {
            return new AutoDisposeCompletable(upstream, scope).subscribeWith(observer);
          }

          @Override public TestObserver<Void> test() {
            TestObserver<Void> observer = new TestObserver<>();
            subscribe(observer);
            return observer;
          }

          @Override public TestObserver<Void> test(boolean cancel) {
            TestObserver<Void> observer = new TestObserver<>();
            if (cancel) {
              observer.cancel();
            }
            subscribe(observer);
            return observer;
          }
        };
      }

      @Override public FlowableSubscribeProxy<T> apply(final Flowable<T> upstream) {
        return new FlowableSubscribeProxy<T>() {
          @Override public Disposable subscribe() {
            return new AutoDisposeFlowable<>(upstream, scope).subscribe();
          }

          @Override public Disposable subscribe(Consumer<? super T> onNext) {
            return new AutoDisposeFlowable<>(upstream, scope).subscribe(onNext);
          }

          @Override public Disposable subscribe(Consumer<? super T> onNext,
              Consumer<? super Throwable> onError) {
            return new AutoDisposeFlowable<>(upstream, scope).subscribe(onNext, onError);
          }

          @Override public Disposable subscribe(Consumer<? super T> onNext,
              Consumer<? super Throwable> onError,
              Action onComplete) {
            return new AutoDisposeFlowable<>(upstream, scope)
                .subscribe(onNext, onError, onComplete);
          }

          @Override public Disposable subscribe(Consumer<? super T> onNext,
              Consumer<? super Throwable> onError,
              Action onComplete,
              Consumer<? super Subscription> onSubscribe) {
            return new AutoDisposeFlowable<>(upstream, scope)
                .subscribe(onNext, onError, onComplete, onSubscribe);
          }

          @Override public void subscribe(Subscriber<T> observer) {
            new AutoDisposeFlowable<>(upstream, scope).subscribe(observer);
          }

          @Override public <E extends Subscriber<? super T>> E subscribeWith(E observer) {
            return new AutoDisposeFlowable<>(upstream, scope).subscribeWith(observer);
          }

          @Override public TestSubscriber<T> test() {
            TestSubscriber<T> ts = new TestSubscriber<>();
            subscribe(ts);
            return ts;
          }

          @Override public TestSubscriber<T> test(long initialRequest) {
            TestSubscriber<T> ts = new TestSubscriber<>(initialRequest);
            subscribe(ts);
            return ts;
          }

          @Override public TestSubscriber<T> test(long initialRequest, boolean cancel) {
            TestSubscriber<T> ts = new TestSubscriber<>(initialRequest);
            if (cancel) {
              ts.cancel();
            }
            subscribe(ts);
            return ts;
          }
        };
      }

      @Override public MaybeSubscribeProxy<T> apply(Maybe<T> upstream) {
        return upstream.to(new MaybeScoper<T>(scope));
      }

      @Override public ObservableSubscribeProxy<T> apply(final Observable<T> upstream) {
        return new ObservableSubscribeProxy<T>() {
          @Override public Disposable subscribe() {
            return new AutoDisposeObservable<>(upstream, scope).subscribe();
          }

          @Override public Disposable subscribe(Consumer<? super T> onNext) {
            return new AutoDisposeObservable<>(upstream, scope).subscribe(onNext);
          }

          @Override public Disposable subscribe(Consumer<? super T> onNext,
              Consumer<? super Throwable> onError) {
            return new AutoDisposeObservable<>(upstream, scope).subscribe(onNext, onError);
          }

          @Override public Disposable subscribe(Consumer<? super T> onNext,
              Consumer<? super Throwable> onError,
              Action onComplete) {
            return new AutoDisposeObservable<>(upstream, scope).subscribe(onNext,
                onError,
                onComplete);
          }

          @Override public Disposable subscribe(Consumer<? super T> onNext,
              Consumer<? super Throwable> onError,
              Action onComplete,
              Consumer<? super Disposable> onSubscribe) {
            return new AutoDisposeObservable<>(upstream, scope).subscribe(onNext,
                onError,
                onComplete,
                onSubscribe);
          }

          @Override public void subscribe(Observer<T> observer) {
            new AutoDisposeObservable<>(upstream, scope).subscribe(observer);
          }

          @Override public <E extends Observer<? super T>> E subscribeWith(E observer) {
            return new AutoDisposeObservable<>(upstream, scope).subscribeWith(observer);
          }

          @Override public TestObserver<T> test() {
            TestObserver<T> observer = new TestObserver<>();
            subscribe(observer);
            return observer;
          }

          @Override public TestObserver<T> test(boolean dispose) {
            TestObserver<T> observer = new TestObserver<>();
            if (dispose) {
              observer.dispose();
            }
            subscribe(observer);
            return observer;
          }
        };
      }

      @Override public SingleSubscribeProxy<T> apply(Single<T> upstream) {
        return upstream.to(new SingleScoper<T>(scope));
      }
    };
  }

  private AutoDispose() {
    throw new AssertionError("No instances");
  }
}
