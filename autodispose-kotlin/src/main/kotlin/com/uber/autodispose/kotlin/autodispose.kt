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

@file:Suppress("NOTHING_TO_INLINE")

package com.uber.autodispose.kotlin

import com.uber.autodispose.AutoDispose
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.ParallelFlowableSubscribeProxy
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.SingleSubscribeProxy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.parallel.ParallelFlowable
import kotlin.DeprecationLevel.ERROR

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(scope)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposeWith(scope: Maybe<*>): FlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(scope)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposeWith(scope: Maybe<*>): ObservableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(scope)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Single<T>.autoDisposeWith(scope: Maybe<*>): SingleSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(scope)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposeWith(scope: Maybe<*>): MaybeSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(scope)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun Completable.autoDisposeWith(scope: Maybe<*>): CompletableSubscribeProxy
    = this.`as`(AutoDispose.autoDisposable<Any>(scope))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposeWith(provider: ScopeProvider): FlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposeWith(provider: ScopeProvider): ObservableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Single<T>.autoDisposeWith(provider: ScopeProvider): SingleSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposeWith(provider: ScopeProvider): MaybeSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun Completable.autoDisposeWith(provider: ScopeProvider): CompletableSubscribeProxy
    = this.`as`(AutoDispose.autoDisposable<Any>(provider))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 *
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposeWith(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): FlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposeWith(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): ObservableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Single<T>.autoDisposeWith(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): SingleSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposeWith(provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): MaybeSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@Deprecated(
    level = ERROR,
    message = "Replaced with autoDisposable() to match top level APIs.",
    replaceWith = ReplaceWith("autoDisposable(provider)",
        "com.uber.autodispose.kotlin.autoDisposable")
)
@CheckReturnValue
inline fun Completable.autoDisposeWith(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): CompletableSubscribeProxy
    = this.`as`(AutoDispose.autoDisposable<Any>(provider))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposable(scope: Maybe<*>): FlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposable(scope: Maybe<*>): ObservableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDisposable(scope: Maybe<*>): SingleSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposable(scope: Maybe<*>): MaybeSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDisposable(scope: Maybe<*>): CompletableSubscribeProxy
    = this.`as`(AutoDispose.autoDisposable<Any>(scope))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDisposable(scope: Maybe<*>): ParallelFlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(scope))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposable(provider: ScopeProvider): FlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposable(provider: ScopeProvider): ObservableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDisposable(provider: ScopeProvider): SingleSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposable(provider: ScopeProvider): MaybeSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDisposable(provider: ScopeProvider): CompletableSubscribeProxy
    = this.`as`(AutoDispose.autoDisposable<Any>(provider))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDisposable(provider: ScopeProvider): ParallelFlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 *
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposable(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): FlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposable(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): ObservableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDisposable(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): SingleSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposable(provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): MaybeSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDisposable(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): CompletableSubscribeProxy
    = this.`as`(AutoDispose.autoDisposable<Any>(provider))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDisposable(
    provider: com.uber.autodispose.lifecycle.LifecycleScopeProvider<*>): ParallelFlowableSubscribeProxy<T>
    = this.`as`(AutoDispose.autoDisposable(provider))
