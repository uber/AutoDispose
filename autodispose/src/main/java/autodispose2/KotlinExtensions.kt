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
@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("KotlinExtensions")

package autodispose2

import autodispose2.Scopes.completableOf
import io.reactivex.rxjava3.annotations.CheckReturnValue
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.parallel.ParallelFlowable

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(scope)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> Flowable<T>.autoDisposable(scope: Completable): FlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDispose(scope: Completable): FlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(scope)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> Observable<T>.autoDisposable(scope: Completable): ObservableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDispose(scope: Completable): ObservableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(scope)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> Single<T>.autoDisposable(scope: Completable): SingleSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDispose(scope: Completable): SingleSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(scope)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> Maybe<T>.autoDisposable(scope: Completable): MaybeSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDispose(scope: Completable): MaybeSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(scope)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun Completable.autoDisposable(scope: Completable): CompletableSubscribeProxy =
    this.to(AutoDispose.autoDisposable<Any>(scope))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDispose(scope: Completable): CompletableSubscribeProxy =
    this.to(AutoDispose.autoDisposable<Any>(scope))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(scope)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> ParallelFlowable<T>.autoDisposable(scope: Completable): ParallelFlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDispose(scope: Completable): ParallelFlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(provider)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> Flowable<T>.autoDisposable(provider: ScopeProvider): FlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDispose(provider: ScopeProvider): FlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(provider)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> Observable<T>.autoDisposable(provider: ScopeProvider): ObservableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDispose(provider: ScopeProvider): ObservableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(provider)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> Single<T>.autoDisposable(provider: ScopeProvider): SingleSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDispose(provider: ScopeProvider): SingleSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(provider)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> Maybe<T>.autoDisposable(provider: ScopeProvider): MaybeSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDispose(provider: ScopeProvider): MaybeSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(provider)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun Completable.autoDisposable(provider: ScopeProvider): CompletableSubscribeProxy =
    this.to(AutoDispose.autoDisposable<Any>(provider))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDispose(provider: ScopeProvider): CompletableSubscribeProxy =
    this.to(AutoDispose.autoDisposable<Any>(provider))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(provider)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T> ParallelFlowable<T>.autoDisposable(provider: ScopeProvider): ParallelFlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDispose(provider: ScopeProvider): ParallelFlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(provider))

/** Executes a [body] with an [AutoDisposeContext] backed by the given [scope]. */
inline fun withScope(scope: ScopeProvider, body: AutoDisposeContext.() -> Unit) {
  withScope(completableOf(scope), body)
}

/** Executes a [body] with an [AutoDisposeContext] backed by the given [completableScope]. */
inline fun withScope(completableScope: Completable, body: AutoDisposeContext.() -> Unit) {
  val context = RealAutoDisposeContext(completableScope)
  context.body()
}

/**
 * A context intended for use as `AutoDisposeContext.() -> Unit` function body parameters
 * where zero-arg [autoDispose] functions can be called. This should be backed by an underlying
 * [Completable] or [ScopeProvider].
 */
interface AutoDisposeContext {
  /** Extension that proxies to the normal [autoDispose] extension function. */
  fun <T> ParallelFlowable<T>.autoDispose(): ParallelFlowableSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function. */
  fun <T> Flowable<T>.autoDispose(): FlowableSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function. */
  fun <T> Observable<T>.autoDispose(): ObservableSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function. */
  fun <T> Single<T>.autoDispose(): SingleSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function. */
  fun <T> Maybe<T>.autoDispose(): MaybeSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function. */
  fun Completable.autoDispose(): CompletableSubscribeProxy
}

@PublishedApi
internal class RealAutoDisposeContext(private val scope: Completable) : AutoDisposeContext {
  override fun <T> ParallelFlowable<T>.autoDispose() = autoDispose(scope)
  override fun <T> Flowable<T>.autoDispose() = autoDispose(scope)
  override fun <T> Observable<T>.autoDispose() = autoDispose(scope)
  override fun <T> Single<T>.autoDispose() = autoDispose(scope)
  override fun <T> Maybe<T>.autoDispose() = autoDispose(scope)
  override fun Completable.autoDispose() = autoDispose(scope)
}
