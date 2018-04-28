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
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

/**
 * Entry point for auto-disposing {@link Maybe}s.
 * <p>
 * The basic flow stencil might look like this:
 * <pre><code>
 *   myThingMaybe
 *        .to(new MaybeScoper<Thing>(...))
 *        .subscribe(...)
 * </code></pre>
 * <p>
 * There are several constructor overloads, with the most basic being a simple {@link
 * #MaybeScoper(Maybe)}. The provided {@link Maybe} is ultimately what every scope resolves to under
 * the hood, and AutoDispose has some built-in understanding for predefined types. The scope is
 * considered ended upon onSuccess emission of this {@link Maybe}. The most common use case would
 * probably be {@link #MaybeScoper(ScopeProvider)}.
 *
 * @param <T> the stream type.
 * @deprecated Use the static factories in {@link AutoDispose}. This will be removed in 1.0.
 */
@Deprecated
public class MaybeScoper<T> extends BaseAutoDisposeConverter
    implements Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> {

  public MaybeScoper(ScopeProvider provider) {
    super(provider);
  }

  public MaybeScoper(LifecycleScopeProvider<?> provider) {
    super(provider);
  }

  public MaybeScoper(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override public MaybeSubscribeProxy<T> apply(final Maybe<? extends T> maybeSource)
      throws Exception {
    return new MaybeSubscribeProxy<T>() {
      @Override public Disposable subscribe() {
        return new AutoDisposeMaybe<>(maybeSource, scope()).subscribe();
      }

      @Override public Disposable subscribe(Consumer<? super T> onNext) {
        return new AutoDisposeMaybe<>(maybeSource, scope()).subscribe(onNext);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return new AutoDisposeMaybe<>(maybeSource, scope()).subscribe(onNext, onError);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
          Action onComplete) {
        return new AutoDisposeMaybe<>(maybeSource, scope()).subscribe(onNext, onError, onComplete);
      }

      @Override public void subscribe(MaybeObserver<T> observer) {
        new AutoDisposeMaybe<>(maybeSource, scope()).subscribe(observer);
      }

      @Override public <E extends MaybeObserver<? super T>> E subscribeWith(E observer) {
        return new AutoDisposeMaybe<>(maybeSource, scope()).subscribeWith(observer);
      }

      @Override public TestObserver<T> test() {
        TestObserver<T> observer = new TestObserver<>();
        subscribe(observer);
        return observer;
      }

      @Override public TestObserver<T> test(boolean cancel) {
        TestObserver<T> observer = new TestObserver<>();

        if (cancel) {
            observer.cancel();
        }
        subscribe(observer);
        return observer;
      }
    };
  }
}

