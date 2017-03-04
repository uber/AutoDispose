/*
 * Copyright (C) 2017. Uber Technologies
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

import com.uber.autodispose.clause.scope.CompletableScopeClause;
import com.uber.autodispose.clause.scope.FlowableScopeClause;
import com.uber.autodispose.clause.scope.MaybeScopeClause;
import com.uber.autodispose.clause.scope.SingleScopeClause;
import com.uber.autodispose.clause.subscribe.CompletableSubscribeClause;
import com.uber.autodispose.clause.subscribe.FlowableSubscribeClause;
import com.uber.autodispose.clause.subscribe.MaybeSubscribeClause;
import com.uber.autodispose.clause.subscribe.SingleSubscribeClause;
import com.uber.autodispose.observers.AutoDisposingCompletableObserver;
import com.uber.autodispose.observers.AutoDisposingMaybeObserver;
import com.uber.autodispose.observers.AutoDisposingSingleObserver;
import com.uber.autodispose.observers.AutoDisposingSubscriber;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import java.util.concurrent.Callable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static com.uber.autodispose.AutoDisposeUtil.checkNotNull;

/**
 * Entry points for creating auto-disposing observers.
 * <p>
 * The top level entry points simply correspond to the RxJava type and serve as entry points to the
 * fluent AutoDispose API for that type. The primary points are:
 * <ul>
 *   <li>{@link #flowable()}
 *   <li>{@link #maybe()}
 *   <li>{@link #single()}
 *   <li>{@link #completable()}
 * </ul>
 * <p>
 * The basic flow stencil might look like this:
 * <pre><code>
 *   myObservable.subscribe(AutoDispose.observable()
 *        .scopeWith(...)
 *        .around(...))
 * </code></pre>
 * <p>
 * There are several overloads for scopeWith(), with the most basic being a simple {@link Maybe}.
 * This {@link Maybe} is ultimately what every scope resolves to under the hood, and AutoDispose has
 * some built in understanding for predefined types. The scope is considered ended upon onSuccess
 * emission of this {@link Maybe}. The most common use case would probably be {@link ScopeProvider}.
 * <p>
 * The second part of AutoDisposal is composing your actual emission logic. Each scope overload
 * returns a subscribe clause that simply mirrors the actual type's top-level subscribe() overloads.
 */
public final class AutoDispose {

  private static final FlowableScopeClause FLOWABLE_SCOPE_CLAUSE = new FlowableClauseImpl();
  private static final MaybeScopeClause MAYBE_SCOPE_CLAUSE = new MaybeScopeClauseImpl();
  private static final SingleScopeClause SINGLE_SCOPE_CLAUSE = new SingleScopeClauseImpl();
  private static final CompletableScopeClause COMPLETABLE_SCOPE_CLAUSE =
      new CompletableScopeClauseImpl();

  private AutoDispose() {
    throw new InstantiationError();
  }

  /**
   * Entry point for AutoDisposing a {@link Flowable}.
   *
   * @return a {@link FlowableScopeClause} for fluent API chaining.
   */
  public static FlowableScopeClause flowable() {
    return FLOWABLE_SCOPE_CLAUSE;
  }

  /**
   * Entry point for AutoDisposing a {@link Maybe}.
   *
   * @return a {@link MaybeScopeClause} for fluent API chaining.
   */
  public static MaybeScopeClause maybe() {
    return MAYBE_SCOPE_CLAUSE;
  }

  /**
   * Entry point for AutoDisposing a {@link Single}.
   *
   * @return a {@link SingleScopeClause} for fluent API chaining.
   */
  public static SingleScopeClause single() {
    return SINGLE_SCOPE_CLAUSE;
  }

  /**
   * Entry point for AutoDisposing a {@link Completable}.
   *
   * @return a {@link CompletableScopeClause} for fluent API chaining.
   */
  public static CompletableScopeClause completable() {
    return COMPLETABLE_SCOPE_CLAUSE;
  }

  private static class FlowableClauseImpl implements FlowableScopeClause {
    @Override public AutoDisposingSubscriberCreator scopeWith(ScopeProvider provider) {
      return new AutoDisposingSubscriberCreator(provider);
    }

    @Override public AutoDisposingSubscriberCreator scopeWith(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingSubscriberCreator(provider);
    }

    @Override public AutoDisposingSubscriberCreator scopeWith(Maybe<?> lifecycle) {
      return new AutoDisposingSubscriberCreator(lifecycle);
    }
  }

  private static class MaybeScopeClauseImpl implements MaybeScopeClause {
    @Override public MaybeSubscribeClause scopeWith(ScopeProvider provider) {
      return new AutoDisposingMaybeObserverCreator(provider);
    }

    @Override public MaybeSubscribeClause scopeWith(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingMaybeObserverCreator(provider);
    }

    @Override public MaybeSubscribeClause scopeWith(Maybe<?> lifecycle) {
      return new AutoDisposingMaybeObserverCreator(lifecycle);
    }
  }

  private static class SingleScopeClauseImpl implements SingleScopeClause {
    @Override public SingleSubscribeClause scopeWith(ScopeProvider provider) {
      return new AutoDisposingSingleObserverCreator(provider);
    }

    @Override public SingleSubscribeClause scopeWith(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingSingleObserverCreator(provider);
    }

    @Override public SingleSubscribeClause scopeWith(Maybe<?> lifecycle) {
      return new AutoDisposingSingleObserverCreator(lifecycle);
    }
  }

  private static class CompletableScopeClauseImpl implements CompletableScopeClause {
    @Override public CompletableSubscribeClause scopeWith(ScopeProvider provider) {
      return new AutoDisposingCompletableObserverCreator(provider);
    }

    @Override public CompletableSubscribeClause scopeWith(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingCompletableObserverCreator(provider);
    }

    @Override public CompletableSubscribeClause scopeWith(Maybe<?> lifecycle) {
      return new AutoDisposingCompletableObserverCreator(lifecycle);
    }
  }

  private static class Base {
    protected final Maybe<?> lifecycle;

    Base(final ScopeProvider provider) {
      this(Maybe.defer(new Callable<MaybeSource<?>>() {
        @Override public MaybeSource<?> call() throws Exception {
          return provider.requestScope();
        }
      }));
    }

    Base(LifecycleScopeProvider<?> provider) {
      this(ScopeUtil.deferredResolvedLifecycle(checkNotNull(provider, "provider == null")));
    }

    Base(Maybe<?> lifecycle) {
      this.lifecycle = checkNotNull(lifecycle, "lifecycle == null");
    }
  }

  private static class AutoDisposingSubscriberCreator extends Base
      implements FlowableSubscribeClause {
    AutoDisposingSubscriberCreator(ScopeProvider provider) {
      super(provider);
    }

    AutoDisposingSubscriberCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingSubscriberCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    @Override public <T> com.uber.autodispose.observers.AutoDisposingSubscriber<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER,
          AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
          AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override public <T> AutoDisposingSubscriber<T> around(Consumer<? super T> onNext) {
      checkNotNull(onNext, "onNext == null");
      return around(onNext, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER, AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override public <T> AutoDisposingSubscriber<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      return around(onNext, onError, AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override public <T> AutoDisposingSubscriber<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError,
        Action onComplete) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      return around(onNext, onError, onComplete, AutoDisposeUtil.EMPTY_SUBSCRIPTION_CONSUMER);
    }

    @Override public <T> AutoDisposingSubscriber<T> around(final Subscriber<T> subscriber) {
      checkNotNull(subscriber, "subscriber == null");
      return around(new Consumer<T>() {
        @Override public void accept(T t1) throws Exception {
          subscriber.onNext(t1);
        }
      }, new Consumer<Throwable>() {
        @Override public void accept(Throwable t) throws Exception {
          subscriber.onError(t);
        }
      }, new Action() {
        @Override public void run() throws Exception {
          subscriber.onComplete();
        }
      }, new Consumer<Subscription>() {
        @Override public void accept(Subscription s) throws Exception {
          subscriber.onSubscribe(s);
        }
      });
    }

    @Override public <T> AutoDisposingSubscriber<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError,
        Action onComplete,
        Consumer<? super Subscription> onSubscribe) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingSubscriberImpl<>(lifecycle, onNext, onError, onComplete, onSubscribe);
    }
  }

  private static class AutoDisposingSingleObserverCreator extends Base
      implements SingleSubscribeClause {
    AutoDisposingSingleObserverCreator(ScopeProvider provider) {
      super(provider);
    }

    AutoDisposingSingleObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingSingleObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    @Override public <T> AutoDisposingSingleObserver<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER);
    }

    @Override public <T> AutoDisposingSingleObserver<T> around(Consumer<? super T> onSuccess) {
      checkNotNull(onSuccess, "onSuccess == null");
      return around(onSuccess, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER);
    }

    @Override
    public <T> AutoDisposingSingleObserver<T> around(final BiConsumer<? super T, ? super
        Throwable> biConsumer) {
      checkNotNull(biConsumer, "biConsumer == null");
      return around(new Consumer<T>() {
        @Override public void accept(T v) throws Exception {
          biConsumer.accept(v, null);
        }
      }, new Consumer<Throwable>() {
        @Override public void accept(Throwable t) throws Exception {
          biConsumer.accept(null, t);
        }
      });
    }

    @Override public <T> AutoDisposingSingleObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      return around(onSuccess, onError, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    @Override public <T> AutoDisposingSingleObserver<T> around(final SingleObserver<T> observer) {
      checkNotNull(observer, "observer == null");
      return around(new Consumer<T>() {
        @Override public void accept(T value) throws Exception {
          observer.onSuccess(value);
        }
      }, new Consumer<Throwable>() {
        @Override public void accept(Throwable e) throws Exception {
          observer.onError(e);
        }
      }, new Consumer<Disposable>() {
        @Override public void accept(Disposable d) throws Exception {
          observer.onSubscribe(d);
        }
      });
    }

    private <T> AutoDisposingSingleObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError,
        Consumer<? super Disposable> onSubscribe) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingSingleObserverImpl<>(lifecycle, onSuccess, onError, onSubscribe);
    }
  }

  private static class AutoDisposingMaybeObserverCreator extends Base
      implements MaybeSubscribeClause {
    AutoDisposingMaybeObserverCreator(ScopeProvider provider) {
      super(provider);
    }

    AutoDisposingMaybeObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingMaybeObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    @Override public <T> AutoDisposingMaybeObserver<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER);
    }

    @Override public <T> AutoDisposingMaybeObserver<T> around(Consumer<? super T> onSuccess) {
      checkNotNull(onSuccess, "onSuccess == null");
      return around(onSuccess,
          AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
          AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override public <T> AutoDisposingMaybeObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      return around(onSuccess, onError, AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override public <T> AutoDisposingMaybeObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError,
        Action onComplete) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      return around(onSuccess, onError, onComplete, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    @Override public <T> AutoDisposingMaybeObserver<T> around(final MaybeObserver<T> observer) {
      checkNotNull(observer, "observer == null");
      return around(new Consumer<T>() {
        @Override public void accept(T value) throws Exception {
          observer.onSuccess(value);
        }
      }, new Consumer<Throwable>() {
        @Override public void accept(Throwable e) throws Exception {
          observer.onError(e);
        }
      }, new Action() {
        @Override public void run() throws Exception {
          observer.onComplete();
        }
      }, new Consumer<Disposable>() {
        @Override public void accept(Disposable d) throws Exception {
          observer.onSubscribe(d);
        }
      });
    }

    private <T> AutoDisposingMaybeObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError,
        Action onComplete,
        Consumer<? super Disposable> onSubscribe) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingMaybeObserverImpl<>(lifecycle,
          onSuccess,
          onError,
          onComplete,
          onSubscribe);
    }
  }

  private static class AutoDisposingCompletableObserverCreator extends Base
      implements CompletableSubscribeClause {
    AutoDisposingCompletableObserverCreator(ScopeProvider provider) {
      super(provider);
    }

    AutoDisposingCompletableObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingCompletableObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    @Override public AutoDisposingCompletableObserver empty() {
      return around(AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override public AutoDisposingCompletableObserver around(Action action) {
      checkNotNull(action, "action == null");
      return around(action, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER);
    }

    @Override public AutoDisposingCompletableObserver around(Action action,
        Consumer<? super Throwable> onError) {
      checkNotNull(action, "action == null");
      checkNotNull(onError, "onError == null");
      return around(action, onError, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    @Override public AutoDisposingCompletableObserver around(final CompletableObserver observer) {
      checkNotNull(observer, "observer == null");
      return around(new Action() {
        @Override public void run() throws Exception {
          observer.onComplete();
        }
      }, new Consumer<Throwable>() {
        @Override public void accept(Throwable e) throws Exception {
          observer.onError(e);
        }
      }, new Consumer<Disposable>() {
        @Override public void accept(Disposable d) throws Exception {
          observer.onSubscribe(d);
        }
      });
    }

    private AutoDisposingCompletableObserver around(Action action,
        Consumer<? super Throwable> onError,
        Consumer<? super Disposable> onSubscribe) {
      checkNotNull(action, "action == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingCompletableObserverImpl(lifecycle, action, onError, onSubscribe);
    }
  }
}
