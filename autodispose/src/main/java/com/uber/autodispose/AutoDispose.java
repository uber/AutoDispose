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

/**
 * Factories for autodispose transformation {@link Function}s that can be used with RxJava types'
 * corresponding {@code to(...)} methods to transform them into auto-disposing streams.
 * <p>
 * There are several static {@code with(...)} entry points, with the most basic being a simple
 * {@link #with(Maybe)}. The provided {@link Maybe} is ultimately what every scope resolves to
 * under the hood, and AutoDispose has some built-in understanding for predefined types. The scope
 * is considered ended upon onSuccess emission of this {@link Maybe}. The most common use case would
 * probably be {@link #with(ScopeProvider)}.
 * <p>
 * Every factory method returns an instance of a {@link ScopeHandler} that serves as an indirection
 * to route to the corresponding RxJava types. This is structured in such a way to be friendly to
 * autocompletion in IDEs, where the no-parameter generic method will autocomplete with the
 * appropriate generic parameters in Java <7, or implicitly in >=8.
 *
 * @see Flowable#to(Function)
 * @see Observable#to(Function)
 * @see Maybe#to(Function)
 * @see Single#to(Function)
 * @see Completable#to(Function)
 * @see ScopeHandler#flowable()
 * @see ScopeHandler#observable()
 * @see ScopeHandler#maybe()
 * @see ScopeHandler#single()
 * @see ScopeHandler#completable()
 */
public final class AutoDispose {

  /**
   * The intermediary return type of the {@code with(...)} factories in {@link AutoDispose}. See the
   * documentation on {@link AutoDispose} for more information on why this interface exists.
   */
  public interface ScopeHandler {
    /**
     * Entry point for auto-disposing {@link Flowable}s.
     * <p>
     * The basic flow stencil might look like this:
     * <pre><code>
     *   myFlowable
     *        .to(AutoDispose.with(scope).<T>flowable())
     *        .subscribe(...)
     * </code></pre>
     *
     * @param <T> the stream type.
     * @return a {@link Function} to transform with {@link Flowable#to(Function)}
     */
    <T> Function<Flowable<? extends T>, FlowableSubscribeProxy<T>> flowable();

    /**
     * Entry point for auto-disposing {@link Observable}s.
     * <p>
     * The basic flow stencil might look like this:
     * <pre><code>
     *   myObservable
     *        .to(AutoDispose.with(scope).<T>observable())
     *        .subscribe(...)
     * </code></pre>
     *
     * @param <T> the stream type.
     * @return a {@link Function} to transform with {@link Observable#to(Function)}
     */
    <T> Function<Observable<? extends T>, ObservableSubscribeProxy<T>> observable();

    /**
     * Entry point for auto-disposing {@link Maybe}s.
     * <p>
     * The basic flow stencil might look like this:
     * <pre><code>
     *   myMaybe
     *        .to(AutoDispose.with(scope).<T>maybe())
     *        .subscribe(...)
     * </code></pre>
     *
     * @param <T> the stream type.
     * @return a {@link Function} to transform with {@link Maybe#to(Function)}
     */
    <T> Function<Maybe<? extends T>, MaybeSubscribeProxy<T>> maybe();

    /**
     * Entry point for auto-disposing {@link Single}s.
     * <p>
     * The basic flow stencil might look like this:
     * <pre><code>
     *   mySingle
     *        .to(AutoDispose.with(scope).<T>single())
     *        .subscribe(...)
     * </code></pre>
     *
     * @param <T> the stream type.
     * @return a {@link Function} to transform with {@link Single#to(Function)}
     */
    <T> Function<Single<? extends T>, SingleSubscribeProxy<T>> single();

    /**
     * Entry point for auto-disposing {@link Completable}s.
     * <p>
     * The basic flow stencil might look like this:
     * <pre><code>
     *   myCompletable
     *        .to(AutoDispose.with(scope).completable())
     *        .subscribe(...)
     * </code></pre>
     *
     * @return a {@link Function} to transform with {@link Completable#to(Function)}
     */
    Function<Completable, CompletableSubscribeProxy> completable();
  }

  /**
   * The factory for {@link Maybe} scopes.
   *
   * @param scope the target scope
   * @return a {@link ScopeHandler} for this scope to create AutoDisposing transformation
   * {@link Function}s
   */
  public static ScopeHandler with(Maybe<?> scope) {
    return new MaybeScopeHandlerImpl(scope);
  }

  /**
   * The factory for {@link ScopeProvider} scopes.
   *
   * @param scope the target scope
   * @return a {@link ScopeHandler} for this scope to create AutoDisposing transformation
   * {@link Function}s
   */
  public static ScopeHandler with(ScopeProvider scope) {
    return new ScopeProviderHandlerImpl(scope);
  }

  /**
   * The factory for {@link LifecycleScopeProvider} scopes.
   *
   * @param scope the target scope
   * @return a {@link ScopeHandler} for this scope to create AutoDisposing transformation
   * {@link Function}s
   */
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

  private AutoDispose() {
    throw new AssertionError("No instances");
  }
}
