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

package autodispose2.android

import android.view.View
import autodispose2.AutoDispose
import autodispose2.CompletableSubscribeProxy
import autodispose2.FlowableSubscribeProxy
import autodispose2.MaybeSubscribeProxy
import autodispose2.ObservableSubscribeProxy
import autodispose2.ParallelFlowableSubscribeProxy
import autodispose2.ScopeProvider
import autodispose2.SingleSubscribeProxy
import io.reactivex.rxjava3.annotations.CheckReturnValue
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.parallel.ParallelFlowable

/**
 * Extension that returns a [ScopeProvider] for this [View].
 */
@CheckReturnValue
inline fun View.scope(): ScopeProvider = ViewScopeProvider.from(this)

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDispose(
  view: View
): FlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDispose(
  view: View
): ObservableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDispose(
  view: View
): SingleSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDispose(
  view: View
): MaybeSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDispose(
  view: View
): CompletableSubscribeProxy =
    this.to(AutoDispose.autoDisposable<Any>(ViewScopeProvider.from(view)))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDispose(
  view: View
): ParallelFlowableSubscribeProxy<T> =
    this.to(AutoDispose.autoDisposable(ViewScopeProvider.from(view)))
