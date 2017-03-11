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

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Entry point for auto-disposing {@link Observable}s.
 * <p>
 * The basic flow stencil might look like this:
 * <pre><code>
 *   myThingObservable
 *        .to(new ObservableScoper<Thing>(...))
 *        .subscribe(...)
 * </code></pre>
 * <p>
 * There are several constructor overloads, with the most basic being a simple {@link
 * #ObservableScoper(Maybe)}. The provided {@link Maybe} is ultimately what every scope resolves to
 * under the hood, and AutoDispose has some built-in understanding for predefined types. The scope
 * is considered ended upon onSuccess emission of this {@link Maybe}. The most common use case would
 * probably be {@link #ObservableScoper(ScopeProvider)}.
 *
 * @param <T> the stream type.
 */
public class ObservableScoper<T> extends Scoper
    implements Function<Observable<? extends T>, ObservableSubscribeProxy<T>> {

  public ObservableScoper(ScopeProvider provider) {
    super(provider);
  }

  public ObservableScoper(LifecycleScopeProvider<?> provider) {
    super(provider);
  }

  public ObservableScoper(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override public ObservableSubscribeProxy<T> apply(final Observable<? extends T> observableSource)
      throws Exception {
    return new ObservableSubscribeProxy<T>() {
      @Override public Disposable subscribe() {
        return new AutoDisposeObservable<>(observableSource, scope()).subscribe();
      }

      @Override public Disposable subscribe(Consumer<? super T> onNext) {
        return new AutoDisposeObservable<>(observableSource, scope()).subscribe(onNext);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return new AutoDisposeObservable<>(observableSource, scope()).subscribe(onNext, onError);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
          Action onComplete) {
        return new AutoDisposeObservable<>(observableSource, scope()).subscribe(onNext, onError,
            onComplete);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
          Action onComplete, Consumer<? super Disposable> onSubscribe) {
        return new AutoDisposeObservable<>(observableSource, scope()).subscribe(onNext, onError,
            onComplete, onSubscribe);
      }

      @Override public void subscribe(Observer<T> observer) {
        new AutoDisposeObservable<>(observableSource, scope()).subscribe(observer);
      }

      @Override public <E extends Observer<? super T>> E subscribeWith(E observer) {
        return new AutoDisposeObservable<>(observableSource, scope()).subscribeWith(observer);
      }
    };
  }

  static final class AutoDisposeObservable<T> extends Observable<T> {
    private final ObservableSource<T> source;
    private final Maybe<?> scope;

    AutoDisposeObservable(ObservableSource<T> source, Maybe<?> scope) {
      this.source = source;
      this.scope = scope;
    }

    @Override protected void subscribeActual(Observer<? super T> observer) {
      source.subscribe(new AutoDisposingObserverImpl<>(scope, observer));
    }
  }
}

