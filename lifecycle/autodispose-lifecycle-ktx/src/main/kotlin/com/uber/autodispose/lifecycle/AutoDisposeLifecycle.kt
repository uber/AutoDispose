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

package com.uber.autodispose.lifecycle

import com.uber.autodispose.AutoDispose
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.ParallelFlowableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.parallel.ParallelFlowable

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable]
 *
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposable(
  provider: LifecycleScopeProvider<*>
): FlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposable(
  provider: LifecycleScopeProvider<*>
): ObservableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDisposable(
  provider: LifecycleScopeProvider<*>
): SingleSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposable(provider: LifecycleScopeProvider<*>): MaybeSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun Completable.autoDisposable(
  provider: LifecycleScopeProvider<*>
): CompletableSubscribeProxy =
    this.`as`(AutoDispose.autoDisposable<Any>(provider))

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable]
 */
@CheckReturnValue
inline fun <T> ParallelFlowable<T>.autoDisposable(
  provider: LifecycleScopeProvider<*>
): ParallelFlowableSubscribeProxy<T> =
    this.`as`(AutoDispose.autoDisposable(provider))
