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
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public final class AutoDispose {

  public interface ScopeHandler {
    <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> flowable();

    <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> observable();

    <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> maybe();

    <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> single();

    Function<Completable, CompletableSubscribeProxy> completable();
  }

  public static ScopeHandler with(Maybe<?> scope) {
    return new MaybeScopeHandlerImpl(scope);
  }

  public static ScopeHandler with(ScopeProvider scope) {
    return new ScopeProviderHandlerImpl(scope);
  }

  public static ScopeHandler with(LifecycleScopeProvider<?> scope) {
    return new LifecycleScopeProviderHandlerImpl(scope);
  }

  private static class MaybeScopeHandlerImpl implements ScopeHandler {

    private final Maybe<?> scope;

    MaybeScopeHandlerImpl(Maybe<?> scope) {
      this.scope = scope;
    }

    @Override public <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> flowable() {
      return new FlowableScoper<>(scope);
    }

    @Override
    public <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> observable() {
      return new ObservableScoper<>(scope);
    }

    @Override public <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> maybe() {
      return new MaybeScoper<>(scope);
    }

    @Override public <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> single() {
      return new SingleScoper<>(scope);
    }

    @Override public Function<Completable, CompletableSubscribeProxy> completable() {
      return new CompletableScoper(scope);
    }
  }

  private static class ScopeProviderHandlerImpl implements ScopeHandler {

    private final ScopeProvider scope;

    ScopeProviderHandlerImpl(ScopeProvider scope) {
      this.scope = scope;
    }

    @Override public <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> flowable() {
      return new FlowableScoper<>(scope);
    }

    @Override
    public <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> observable() {
      return new ObservableScoper<>(scope);
    }

    @Override public <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> maybe() {
      return new MaybeScoper<>(scope);
    }

    @Override public <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> single() {
      return new SingleScoper<>(scope);
    }

    @Override public Function<Completable, CompletableSubscribeProxy> completable() {
      return new CompletableScoper(scope);
    }
  }

  private static class LifecycleScopeProviderHandlerImpl implements ScopeHandler {

    private final LifecycleScopeProvider<?> scope;

    LifecycleScopeProviderHandlerImpl(LifecycleScopeProvider<?> scope) {
      this.scope = scope;
    }

    @Override public <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> flowable() {
      return new FlowableScoper<>(scope);
    }

    @Override
    public <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> observable() {
      return new ObservableScoper<>(scope);
    }

    @Override public <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> maybe() {
      return new MaybeScoper<>(scope);
    }

    @Override public <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> single() {
      return new SingleScoper<>(scope);
    }

    @Override public Function<Completable, CompletableSubscribeProxy> completable() {
      return new CompletableScoper(scope);
    }
  }
}
