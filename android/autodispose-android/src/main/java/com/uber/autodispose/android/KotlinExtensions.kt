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

package com.uber.autodispose.android

import android.view.View
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.ParallelFlowableSubscribeProxy
import com.uber.autodispose.ScopeProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.Maybe
import io.reactivex.Completable
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.parallel.ParallelFlowable

/**
 * Extension that returns a [ScopeProvider] for this [View].
 */
@CheckReturnValue
inline fun View.scope(): ScopeProvider = ViewScopeProvider.from(this)

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(replaceWith = ReplaceWith("autoDispose(view)"), message = "Renamed to `autoDispose()`", level = DeprecationLevel.ERROR)
inline fun <T> Flowable<T>.autoDisposable(
  view: View
): FlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDispose(
  view: View
): FlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(replaceWith = ReplaceWith("autoDispose(view)"), message = "Renamed to `autoDispose()`", level = DeprecationLevel.ERROR)
inline fun <T> Observable<T>.autoDisposable(
  view: View
): ObservableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDispose(
  view: View
): ObservableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(replaceWith = ReplaceWith("autoDispose(view)"), message = "Renamed to `autoDispose()`", level = DeprecationLevel.ERROR)
inline fun <T> Single<T>.autoDisposable(
  view: View
): SingleSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDispose(
  view: View
): SingleSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(replaceWith = ReplaceWith("autoDispose(view)"), message = "Renamed to `autoDispose()`", level = DeprecationLevel.ERROR)
inline fun <T> Maybe<T>.autoDisposable(
  view: View
): MaybeSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDispose(
  view: View
): MaybeSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(replaceWith = ReplaceWith("autoDispose(view)"), message = "Renamed to `autoDispose()`", level = DeprecationLevel.ERROR)
inline fun Completable.autoDisposable(
  view: View
): CompletableSubscribeProxy =
    this.`as`(AutoDispose.autoDisposable<Any>(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDispose(
  view: View
): CompletableSubscribeProxy =
    this.`as`(AutoDispose.autoDisposable<Any>(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
@Deprecated(replaceWith = ReplaceWith("autoDispose(view)"), message = "Renamed to `autoDispose()`", level = DeprecationLevel.ERROR)
inline fun <T> ParallelFlowable<T>.autoDisposable(
  view: View
): ParallelFlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDispose(
  view: View
): ParallelFlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))
