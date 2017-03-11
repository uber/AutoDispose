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
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Entry point for auto-disposing {@link Single}s.
 * <p>
 * The basic flow stencil might look like this:
 * <pre><code>
 *   myThingSingle
 *        .to(new SingleScoper<Thing>(...))
 *        .subscribe(...)
 * </code></pre>
 * <p>
 * There are several constructor overloads, with the most basic being a simple {@link
 * #SingleScoper(Maybe)}. The provided {@link Maybe} is ultimately what every scope resolves to
 * under the hood, and AutoDispose has some built-in understanding for predefined types. The scope
 * is considered ended upon onSuccess emission of this {@link Maybe}. The most common use case would
 * probably be {@link #SingleScoper(ScopeProvider)}.
 *
 * @param <T> the stream type.
 */
public class SingleScoper<T> extends Scoper
    implements Function<Single<? extends T>, SingleSubscribeProxy<T>> {

  public SingleScoper(ScopeProvider provider) {
    super(provider);
  }

  public SingleScoper(LifecycleScopeProvider<?> provider) {
    super(provider);
  }

  public SingleScoper(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override public SingleSubscribeProxy<T> apply(final Single<? extends T> singleSource)
      throws Exception {
    return new SingleSubscribeProxy<T>() {
      @Override public Disposable subscribe() {
        return new AutoDisposeSingle<>(singleSource, scope()).subscribe();
      }

      @Override public Disposable subscribe(Consumer<? super T> onNext) {
        return new AutoDisposeSingle<>(singleSource, scope()).subscribe(onNext);
      }

      @Override public Disposable subscribe(BiConsumer<? super T, ? super Throwable> biConsumer) {
        return new AutoDisposeSingle<>(singleSource, scope()).subscribe(biConsumer);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return new AutoDisposeSingle<>(singleSource, scope()).subscribe(onNext, onError);
      }

      @Override public void subscribe(SingleObserver<T> observer) {
        new AutoDisposeSingle<>(singleSource, scope()).subscribe(observer);
      }

      @Override public <E extends SingleObserver<? super T>> E subscribeWith(E observer) {
        return new AutoDisposeSingle<>(singleSource, scope()).subscribeWith(observer);
      }
    };
  }

  static final class AutoDisposeSingle<T> extends Single<T> {
    private final SingleSource<T> source;
    private final Maybe<?> scope;

    AutoDisposeSingle(SingleSource<T> source, Maybe<?> scope) {
      this.source = source;
      this.scope = scope;
    }

    @Override protected void subscribeActual(SingleObserver<? super T> observer) {
      source.subscribe(new AutoDisposingSingleObserverImpl<>(scope, observer));
    }
  }
}

