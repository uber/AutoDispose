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
import io.reactivex.MaybeSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MaybeScoper<T> extends Scoper
    implements Function<Maybe<T>, MaybeSubscribeProxy<T>> {

  public MaybeScoper(ScopeProvider provider) {
    super(provider);
  }

  public MaybeScoper(LifecycleScopeProvider<?> provider) {
    super(provider);
  }

  public MaybeScoper(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override public MaybeSubscribeProxy<T> apply(final Maybe<T> maybeSource) throws Exception {
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
    };
  }

  static final class AutoDisposeMaybe<T> extends Maybe<T> {
    private final MaybeSource<T> source;
    private final Maybe<?> scope;

    AutoDisposeMaybe(MaybeSource<T> source, Maybe<?> scope) {
      this.source = source;
      this.scope = scope;
    }

    @Override protected void subscribeActual(MaybeObserver<? super T> observer) {
      source.subscribe(new AutoDisposingMaybeObserverImpl<>(scope, observer));
    }
  }
}

