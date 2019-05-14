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

package com.uber.autodispose.android.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleOwner
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.ParallelFlowableSubscribeProxy
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.android.lifecycle.internal.CorrespondingEventsUtil.DEFAULT_CORRESPONDING_EVENTS
import com.uber.autodispose.android.lifecycle.internal.CorrespondingEventsUtil.getCorrespondingEvent
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.parallel.ParallelFlowable
import io.reactivex.Observable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.Completable

/**
 * Extension that returns a [ScopeProvider] for this [LifecycleOwner].
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(): ScopeProvider = AndroidLifecycleScopeProvider.from(this)

/**
 * Extension that returns a [ScopeProvider] for this [LifecycleOwner].
 *
 * @param untilEvent the event until the scope is valid.
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(
  untilEvent: Lifecycle.Event
): ScopeProvider = AndroidLifecycleScopeProvider.from(this, untilEvent)

/**
 * Extension that returns a [ScopeProvider] for this [LifecycleOwner].
 *
 * @param boundaryResolver function that resolves the event boundary.
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(
  boundaryResolver: CorrespondingEventsFunction<Event>
): ScopeProvider = AndroidLifecycleScopeProvider.from(this, boundaryResolver)

/**
 * Extension that returns a [ScopeProvider] for this [Lifecycle].
 */
@CheckReturnValue
inline fun Lifecycle.scope(): ScopeProvider = AndroidLifecycleScopeProvider.from(this)

/**
 * Extension that returns a [ScopeProvider] for this [Lifecycle].
 *
 * @param untilEvent the event until the scope is valid.
 */
@CheckReturnValue
inline fun Lifecycle.scope(
  untilEvent: Lifecycle.Event
): ScopeProvider = AndroidLifecycleScopeProvider.from(
    this, untilEvent)

/**
 * Extension that returns a [ScopeProvider] for this [Lifecycle].
 *
 * @param boundaryResolver function that resolves the event boundary.
 */
@CheckReturnValue
inline fun Lifecycle.scope(
  boundaryResolver: CorrespondingEventsFunction<Event>
): ScopeProvider = AndroidLifecycleScopeProvider.from(
    this, boundaryResolver)

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposable(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event = DEFAULT_CORRESPONDING_EVENTS.apply(getCorrespondingEvent(lifecycleOwner.lifecycle))
): FlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent)))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposable(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event = DEFAULT_CORRESPONDING_EVENTS.apply(getCorrespondingEvent(lifecycleOwner.lifecycle))
): ObservableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent)))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDisposable(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event = DEFAULT_CORRESPONDING_EVENTS.apply(getCorrespondingEvent(lifecycleOwner.lifecycle))
): SingleSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent)))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposable(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event = DEFAULT_CORRESPONDING_EVENTS.apply(getCorrespondingEvent(lifecycleOwner.lifecycle))
): MaybeSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent)))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDisposable(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event = DEFAULT_CORRESPONDING_EVENTS.apply(getCorrespondingEvent(lifecycleOwner.lifecycle))
): CompletableSubscribeProxy =
    this.`as`(AutoDispose.autoDisposable<Any>(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent)))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDisposable(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event = DEFAULT_CORRESPONDING_EVENTS.apply(getCorrespondingEvent(lifecycleOwner.lifecycle))
): ParallelFlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent)))
