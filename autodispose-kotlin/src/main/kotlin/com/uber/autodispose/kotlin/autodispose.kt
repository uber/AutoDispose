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
import com.uber.autodispose.AutoDispose.ScopeHandler
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.LifecycleScopeProvider
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.SingleSubscribeProxy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.annotations.CheckReturnValue

/**
 * Extension that proxies to [Flowable.to] + [AutoDispose.with] + [ScopeHandler.forFlowable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposeWith(scope: Maybe<*>): FlowableSubscribeProxy<T>
    = this.to(AutoDispose.with(scope).forFlowable())

/**
 * Extension that proxies to [Observable.to] + [AutoDispose.with] + [ScopeHandler.forObservable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposeWith(scope: Maybe<*>): ObservableSubscribeProxy<T>
    = this.to(AutoDispose.with(scope).forObservable())

/**
 * Extension that proxies to [Single.to] + [AutoDispose.with] + [ScopeHandler.forSingle]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDisposeWith(scope: Maybe<*>): SingleSubscribeProxy<T>
    = this.to(AutoDispose.with(scope).forSingle())

/**
 * Extension that proxies to [Maybe.to] + [AutoDispose.with] + [ScopeHandler.forMaybe]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposeWith(scope: Maybe<*>): MaybeSubscribeProxy<T>
    = this.to(AutoDispose.with(scope).forMaybe())

/**
 * Extension that proxies to [Completable.to] + [AutoDispose.with] + [ScopeHandler.forCompletable]
 */
@CheckReturnValue
inline fun Completable.autoDisposeWith(scope: Maybe<*>): CompletableSubscribeProxy
    = this.to(AutoDispose.with(scope).forCompletable())

/**
 * Extension that proxies to [Flowable.to] + [AutoDispose.with] + [ScopeHandler.forFlowable]
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposeWith(provider: ScopeProvider): FlowableSubscribeProxy<T>
    = this.to(AutoDispose.with(provider).forFlowable())

/**
 * Extension that proxies to [Observable.to] + [AutoDispose.with] + [ScopeHandler.forObservable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposeWith(provider: ScopeProvider): ObservableSubscribeProxy<T>
    = this.to(AutoDispose.with(provider).forObservable())

/**
 * Extension that proxies to [Single.to] + [AutoDispose.with] + [ScopeHandler.forSingle]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDisposeWith(provider: ScopeProvider): SingleSubscribeProxy<T>
    = this.to(AutoDispose.with(provider).forSingle())

/**
 * Extension that proxies to [Maybe.to] + [AutoDispose.with] + [ScopeHandler.forMaybe]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposeWith(provider: ScopeProvider): MaybeSubscribeProxy<T>
    = this.to(AutoDispose.with(provider).forMaybe())

/**
 * Extension that proxies to [Completable.to] + [AutoDispose.with] + [ScopeHandler.forCompletable]
 */
@CheckReturnValue
inline fun Completable.autoDisposeWith(provider: ScopeProvider): CompletableSubscribeProxy
    = this.to(AutoDispose.with(provider).forCompletable())

/**
 * Extension that proxies to [Flowable.to] + [AutoDispose.with]+ [ScopeHandler.forFlowable]
 *
 */
@CheckReturnValue
inline fun <T> Flowable<T>.autoDisposeWith(
    provider: LifecycleScopeProvider<*>): FlowableSubscribeProxy<T>
    = this.to(AutoDispose.with(provider).forFlowable())

/**
 * Extension that proxies to [Observable.to] + [AutoDispose.with] + [ScopeHandler.forObservable]
 */
@CheckReturnValue
inline fun <T> Observable<T>.autoDisposeWith(
    provider: LifecycleScopeProvider<*>): ObservableSubscribeProxy<T>
    = this.to(AutoDispose.with(provider).forObservable())

/**
 * Extension that proxies to [Single.to] + [AutoDispose.with] + [ScopeHandler.forSingle]
 */
@CheckReturnValue
inline fun <T> Single<T>.autoDisposeWith(
    provider: LifecycleScopeProvider<*>): SingleSubscribeProxy<T>
    = this.to(AutoDispose.with(provider).forSingle())

/**
 * Extension that proxies to [Maybe.to] + [AutoDispose.with] + [ScopeHandler.forMaybe]
 */
@CheckReturnValue
inline fun <T> Maybe<T>.autoDisposeWith(provider: LifecycleScopeProvider<*>): MaybeSubscribeProxy<T>
    = this.to(AutoDispose.with(provider).forMaybe())

/**
 * Extension that proxies to [Completable.to] + [AutoDispose.with]+ [ScopeHandler.forCompletable]
 */
@CheckReturnValue
inline fun Completable.autoDisposeWith(
    provider: LifecycleScopeProvider<*>): CompletableSubscribeProxy
    = this.to(AutoDispose.with(provider).forCompletable())

