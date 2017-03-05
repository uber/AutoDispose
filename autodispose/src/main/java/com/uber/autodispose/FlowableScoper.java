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

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Entry point for auto-disposing {@link Flowable}s.
 * <p>
 * The basic flow stencil might look like this:
 * <pre><code>
 *   myThingFlowable
 *        .to(new FlowableScoper<Thing>(...))
 *        .subscribe(...)
 * </code></pre>
 * <p>
 * There are several constructor overloads, with the most basic being a simple {@link
 * #FlowableScoper(Maybe)}. The provided {@link Maybe} is ultimately what every scope resolves to
 * under the hood, and AutoDispose has some built-in understanding for predefined types. The scope
 * is considered ended upon onSuccess emission of this {@link Maybe}. The most common use case would
 * probably be {@link #FlowableScoper(ScopeProvider)}.
 *
 * @param <T> the stream type.
 */
public class FlowableScoper<T> extends Scoper
    implements Function<Flowable<T>, FlowableSubscribeProxy<T>> {

  /**
   * {@inheritDoc}
   */
  public FlowableScoper(ScopeProvider provider) {
    super(provider);
  }

  /**
   * {@inheritDoc}
   */
  public FlowableScoper(LifecycleScopeProvider<?> provider) {
    super(provider);
  }

  /**
   * {@inheritDoc}
   */
  public FlowableScoper(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override public FlowableSubscribeProxy<T> apply(final Flowable<T> source) throws Exception {
    return new FlowableSubscribeProxy<T>() {
      @Override public Disposable subscribe() {
        return new AutoDisposeFlowable<>(source, scope()).subscribe();
      }

      @Override public Disposable subscribe(Consumer<? super T> onNext) {
        return new AutoDisposeFlowable<>(source, scope()).subscribe(onNext);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return new AutoDisposeFlowable<>(source, scope()).subscribe(onNext, onError);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
          Action onComplete) {
        return new AutoDisposeFlowable<>(source, scope()).subscribe(onNext, onError, onComplete);
      }

      @Override
      public Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
          Action onComplete, Consumer<? super Subscription> onSubscribe) {
        return new AutoDisposeFlowable<>(source, scope()).subscribe(onNext, onError, onComplete,
            onSubscribe);
      }

      @Override public void subscribe(Subscriber<T> observer) {
        new AutoDisposeFlowable<>(source, scope()).subscribe(observer);
      }

      @Override public <E extends Subscriber<? super T>> E subscribeWith(E observer) {
        return new AutoDisposeFlowable<>(source, scope()).subscribeWith(observer);
      }
    };
  }

  static final class AutoDisposeFlowable<T> extends Flowable<T> {
    private final Publisher<T> source;
    private final Maybe<?> scope;

    AutoDisposeFlowable(Publisher<T> source, Maybe<?> scope) {
      this.source = source;
      this.scope = scope;
    }

    @Override protected void subscribeActual(Subscriber<? super T> observer) {
      source.subscribe(new AutoDisposingSubscriberImpl<>(scope, observer));
    }
  }
}

