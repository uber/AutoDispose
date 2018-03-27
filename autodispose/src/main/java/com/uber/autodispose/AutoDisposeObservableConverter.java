/*
 * Copyright (c) 2018. Uber Technologies
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
 *
 */

package com.uber.autodispose;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableConverter;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;

class AutoDisposeObservableConverter<T> extends BaseAutoDisposeConverter
    implements ObservableConverter<T, ObservableSubscribeProxy<T>> {

  public AutoDisposeObservableConverter(ScopeProvider provider) {
    super(provider);
  }

  public AutoDisposeObservableConverter(LifecycleScopeProvider<?> provider) {
    super(provider);
  }

  public AutoDisposeObservableConverter(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override public ObservableSubscribeProxy<T> apply(final Observable<T> upstream) {
    return new ObservableSubscribeProxy<T>() {
      @Override public Disposable subscribe() {
        return new AutoDisposeObservable<>(upstream, scope()).subscribe();
      }

      @Override public Disposable subscribe(Consumer<? super T> onNext) {
        return new AutoDisposeObservable<>(upstream, scope()).subscribe(onNext);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return new AutoDisposeObservable<>(upstream, scope()).subscribe(onNext, onError);
      }

      @Override public Disposable subscribe(Consumer<? super T> onNext,
          Consumer<? super Throwable> onError,
          Action onComplete) {
        return new AutoDisposeObservable<>(upstream, scope()).subscribe(onNext,
            onError,
            onComplete);
      }

      @Override public Disposable subscribe(Consumer<? super T> onNext,
          Consumer<? super Throwable> onError,
          Action onComplete,
          Consumer<? super Disposable> onSubscribe) {
        return new AutoDisposeObservable<>(upstream, scope()).subscribe(onNext,
            onError,
            onComplete,
            onSubscribe);
      }

      @Override public void subscribe(Observer<T> observer) {
        new AutoDisposeObservable<>(upstream, scope()).subscribe(observer);
      }

      @Override public <E extends Observer<? super T>> E subscribeWith(E observer) {
        return new AutoDisposeObservable<>(upstream, scope()).subscribeWith(observer);
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

