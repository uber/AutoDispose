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

package com.uber.autodispose

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.parallel.ParallelFlowable

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
    this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDispose(scope: Completable): FlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(scope))

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
    this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDispose(scope: Completable): ObservableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(scope))

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
    this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDispose(scope: Completable): SingleSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(scope))

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
    this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDispose(scope: Completable): MaybeSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(scope))

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
    this.`as`(AutoDispose.autoDisposable<Any>(scope))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDispose(scope: Completable): CompletableSubscribeProxy =
    this.`as`(AutoDispose.autoDisposable<Any>(scope))

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
    this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDispose(scope: Completable): ParallelFlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(scope))

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
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDispose(provider: ScopeProvider): FlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))

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
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDispose(provider: ScopeProvider): ObservableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))

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
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDispose(provider: ScopeProvider): SingleSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))

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
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDispose(provider: ScopeProvider): MaybeSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))

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
    this.`as`(AutoDispose.autoDisposable<Any>(provider))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDispose(provider: ScopeProvider): CompletableSubscribeProxy =
    this.`as`(AutoDispose.autoDisposable<Any>(provider))

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
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDispose(provider: ScopeProvider): ParallelFlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))
