/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2;

import static autodispose2.AutoDisposeUtil.checkNotNull;
import static autodispose2.Scopes.completableOf;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableConverter;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.reactivestreams.Subscriber;

/**
 * Factories for autodispose converters that can be used with RxJava types' corresponding {@code
 * as(...)} methods to transform them into auto-disposing streams.
 *
 * <p>There are several static {@code autoDisposable(...)} entry points, with the most basic being a
 * simple {@link #autoDisposable(CompletableSource)}. The provided {@link CompletableSource} is
 * ultimately what every scope resolves to under the hood, and AutoDispose has some built-in
 * understanding for predefined types. The scope is considered ended upon onComplete emission of
 * this {@link Completable}.
 *
 * <p>This is structured in such a way to be friendly to autocompletion in IDEs, where the
 * no-parameter generic method will autocomplete with the appropriate generic parameters in Java <7,
 * or implicitly in >=8.
 *
 * @see Flowable#to(io.reactivex.rxjava3.core.FlowableConverter)
 * @see Observable#to(io.reactivex.rxjava3.core.ObservableConverter)
 * @see Maybe#to(io.reactivex.rxjava3.core.MaybeConverter)
 * @see Single#to(io.reactivex.rxjava3.core.SingleConverter)
 * @see Completable#to(io.reactivex.rxjava3.core.CompletableConverter)
 */
public final class AutoDispose {

  /**
   * Entry point for auto-disposing streams from a {@link ScopeProvider}.
   *
   * <p>Example usage:
   *
   * <pre><code>
   *   Observable.just(1)
   *        .to(autoDisposable(scope)) // Static import
   *        .subscribe(...)
   * </code></pre>
   *
   * @param provider the target scope provider
   * @param <T> the stream type.
   * @return an {@link AutoDisposeConverter} to transform with operators like {@link
   *     Observable#to(ObservableConverter)}
   */
  public static <@NonNull T> AutoDisposeConverter<T> autoDisposable(final ScopeProvider provider) {
    checkNotNull(provider, "provider == null");
    return autoDisposable(completableOf(provider));
  }

  /**
   * Entry point for auto-disposing streams from a {@link CompletableSource}.
   *
   * <p>Example usage:
   *
   * <pre><code>
   *   Observable.just(1)
   *        .to(autoDisposable(scope)) // Static import
   *        .subscribe(...)
   * </code></pre>
   *
   * @param scope the target scope
   * @param <T> the stream type.
   * @return an {@link AutoDisposeConverter} to transform with operators like {@link
   *     Observable#to(ObservableConverter)}
   */
  public static <@NonNull T> AutoDisposeConverter<T> autoDisposable(final CompletableSource scope) {
    checkNotNull(scope, "scope == null");
    return new AutoDisposeConverter<T>() {
      @Override
      public ParallelFlowableSubscribeProxy<T> apply(final ParallelFlowable<T> upstream) {
        if (!AutoDisposePlugins.hideProxies) {
          return new AutoDisposeParallelFlowable<>(upstream, scope);
        }
        return subscribers ->
            new AutoDisposeParallelFlowable<>(upstream, scope).subscribe(subscribers);
      }

      @Override
      public CompletableSubscribeProxy apply(final Completable upstream) {
        if (!AutoDisposePlugins.hideProxies) {
          return new AutoDisposeCompletable(upstream, scope);
        }
        return new CompletableSubscribeProxy() {
          @Override
          public Disposable subscribe() {
            return new AutoDisposeCompletable(upstream, scope).subscribe();
          }

          @Override
          public Disposable subscribe(Action action) {
            return new AutoDisposeCompletable(upstream, scope).subscribe(action);
          }

          @Override
          public Disposable subscribe(Action action, Consumer<? super Throwable> onError) {
            return new AutoDisposeCompletable(upstream, scope).subscribe(action, onError);
          }

          @Override
          public void subscribe(CompletableObserver observer) {
            new AutoDisposeCompletable(upstream, scope).subscribe(observer);
          }

          @Override
          public <@NonNull E extends CompletableObserver> E subscribeWith(E observer) {
            return new AutoDisposeCompletable(upstream, scope).subscribeWith(observer);
          }

          @Override
          public TestObserver<Void> test() {
            TestObserver<Void> observer = new TestObserver<>();
            subscribe(observer);
            return observer;
          }

          @Override
          public TestObserver<Void> test(boolean dispose) {
            TestObserver<Void> observer = new TestObserver<>();
            if (dispose) {
              observer.dispose();
            }
            subscribe(observer);
            return observer;
          }
        };
      }

      @Override
      public FlowableSubscribeProxy<T> apply(final Flowable<T> upstream) {
        if (!AutoDisposePlugins.hideProxies) {
          return new AutoDisposeFlowable<>(upstream, scope);
        }
        return new FlowableSubscribeProxy<T>() {
          @Override
          public Disposable subscribe() {
            return new AutoDisposeFlowable<>(upstream, scope).subscribe();
          }

          @Override
          public Disposable subscribe(Consumer<? super T> onNext) {
            return new AutoDisposeFlowable<>(upstream, scope).subscribe(onNext);
          }

          @Override
          public Disposable subscribe(
              Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
            return new AutoDisposeFlowable<>(upstream, scope).subscribe(onNext, onError);
          }

          @Override
          public Disposable subscribe(
              Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {
            return new AutoDisposeFlowable<>(upstream, scope)
                .subscribe(onNext, onError, onComplete);
          }

          @Override
          public void subscribe(Subscriber<? super T> observer) {
            new AutoDisposeFlowable<>(upstream, scope).subscribe(observer);
          }

          @Override
          public <@NonNull E extends Subscriber<? super T>> E subscribeWith(E observer) {
            return new AutoDisposeFlowable<>(upstream, scope).subscribeWith(observer);
          }

          @Override
          public TestSubscriber<T> test() {
            TestSubscriber<T> ts = new TestSubscriber<>();
            subscribe(ts);
            return ts;
          }

          @Override
          public TestSubscriber<T> test(long initialRequest) {
            TestSubscriber<T> ts = new TestSubscriber<>(initialRequest);
            subscribe(ts);
            return ts;
          }

          @Override
          public TestSubscriber<T> test(long initialRequest, boolean cancel) {
            TestSubscriber<T> ts = new TestSubscriber<>(initialRequest);
            if (cancel) {
              ts.cancel();
            }
            subscribe(ts);
            return ts;
          }
        };
      }

      @Override
      public MaybeSubscribeProxy<T> apply(final Maybe<T> upstream) {
        if (!AutoDisposePlugins.hideProxies) {
          return new AutoDisposeMaybe<>(upstream, scope);
        }
        return new MaybeSubscribeProxy<T>() {
          @Override
          public Disposable subscribe() {
            return new AutoDisposeMaybe<>(upstream, scope).subscribe();
          }

          @Override
          public Disposable subscribe(Consumer<? super T> onSuccess) {
            return new AutoDisposeMaybe<>(upstream, scope).subscribe(onSuccess);
          }

          @Override
          public Disposable subscribe(
              Consumer<? super T> onSuccess, Consumer<? super Throwable> onError) {
            return new AutoDisposeMaybe<>(upstream, scope).subscribe(onSuccess, onError);
          }

          @Override
          public Disposable subscribe(
              Consumer<? super T> onSuccess,
              Consumer<? super Throwable> onError,
              Action onComplete) {
            return new AutoDisposeMaybe<>(upstream, scope)
                .subscribe(onSuccess, onError, onComplete);
          }

          @Override
          public void subscribe(MaybeObserver<? super T> observer) {
            new AutoDisposeMaybe<>(upstream, scope).subscribe(observer);
          }

          @Override
          public <@NonNull E extends MaybeObserver<? super T>> E subscribeWith(E observer) {
            return new AutoDisposeMaybe<>(upstream, scope).subscribeWith(observer);
          }

          @Override
          public TestObserver<T> test() {
            TestObserver<T> observer = new TestObserver<>();
            subscribe(observer);
            return observer;
          }

          @Override
          public TestObserver<T> test(boolean dispose) {
            TestObserver<T> observer = new TestObserver<>();
            if (dispose) {
              observer.dispose();
            }
            subscribe(observer);
            return observer;
          }
        };
      }

      @Override
      public ObservableSubscribeProxy<T> apply(final Observable<T> upstream) {
        if (!AutoDisposePlugins.hideProxies) {
          return new AutoDisposeObservable<>(upstream, scope);
        }
        return new ObservableSubscribeProxy<T>() {
          @Override
          public Disposable subscribe() {
            return new AutoDisposeObservable<>(upstream, scope).subscribe();
          }

          @Override
          public Disposable subscribe(Consumer<? super T> onNext) {
            return new AutoDisposeObservable<>(upstream, scope).subscribe(onNext);
          }

          @Override
          public Disposable subscribe(
              Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
            return new AutoDisposeObservable<>(upstream, scope).subscribe(onNext, onError);
          }

          @Override
          public Disposable subscribe(
              Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {
            return new AutoDisposeObservable<>(upstream, scope)
                .subscribe(onNext, onError, onComplete);
          }

          @Override
          public void subscribe(Observer<? super T> observer) {
            new AutoDisposeObservable<>(upstream, scope).subscribe(observer);
          }

          @Override
          public <@NonNull E extends Observer<? super T>> E subscribeWith(E observer) {
            return new AutoDisposeObservable<>(upstream, scope).subscribeWith(observer);
          }

          @Override
          public TestObserver<T> test() {
            TestObserver<T> observer = new TestObserver<>();
            subscribe(observer);
            return observer;
          }

          @Override
          public TestObserver<T> test(boolean dispose) {
            TestObserver<T> observer = new TestObserver<>();
            if (dispose) {
              observer.dispose();
            }
            subscribe(observer);
            return observer;
          }
        };
      }

      @Override
      public SingleSubscribeProxy<T> apply(final Single<T> upstream) {
        if (!AutoDisposePlugins.hideProxies) {
          return new AutoDisposeSingle<>(upstream, scope);
        }
        return new SingleSubscribeProxy<T>() {
          @Override
          public Disposable subscribe() {
            return new AutoDisposeSingle<>(upstream, scope).subscribe();
          }

          @Override
          public Disposable subscribe(Consumer<? super T> onSuccess) {
            return new AutoDisposeSingle<>(upstream, scope).subscribe(onSuccess);
          }

          @Override
          public Disposable subscribe(BiConsumer<@Nullable ? super T, @Nullable ? super Throwable> biConsumer) {
            return new AutoDisposeSingle<>(upstream, scope).subscribe(biConsumer);
          }

          @Override
          public Disposable subscribe(
              Consumer<? super T> onSuccess, Consumer<? super Throwable> onError) {
            return new AutoDisposeSingle<>(upstream, scope).subscribe(onSuccess, onError);
          }

          @Override
          public void subscribe(SingleObserver<? super T> observer) {
            new AutoDisposeSingle<>(upstream, scope).subscribe(observer);
          }

          @Override
          public <@NonNull E extends SingleObserver<? super T>> E subscribeWith(E observer) {
            return new AutoDisposeSingle<>(upstream, scope).subscribeWith(observer);
          }

          @Override
          public TestObserver<T> test() {
            TestObserver<T> observer = new TestObserver<>();
            subscribe(observer);
            return observer;
          }

          @Override
          public TestObserver<T> test(boolean dispose) {
            TestObserver<T> observer = new TestObserver<>();
            if (dispose) {
              observer.dispose();
            }
            subscribe(observer);
            return observer;
          }
        };
      }
    };
  }

  private AutoDispose() {
    throw new AssertionError("No instances");
  }
}
