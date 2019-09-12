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
package com.ubercab.autodispose.rxlifecycle

import com.trello.rxlifecycle2.LifecycleProvider
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.ParallelFlowableSubscribeProxy
import com.uber.autodispose.ScopeProvider
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.Completable
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.parallel.ParallelFlowable

/**
 * Extension that converts a [LifecycleProvider] to [ScopeProvider].
 */
inline fun <E> LifecycleProvider<E>.scope(event: E? = null): ScopeProvider {
  return if (event == null) {
    RxLifecycleInterop.from(this)
  } else {
    RxLifecycleInterop.from(this, event)
  }
}

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(lifecycleProvider, event)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T, E> Flowable<T>.autoDisposable(lifecycleProvider: LifecycleProvider<E>, event: E? = null): FlowableSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
inline fun <T, E> Flowable<T>.autoDispose(lifecycleProvider: LifecycleProvider<E>, event: E? = null): FlowableSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(lifecycleProvider, event)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T, E> Observable<T>.autoDisposable(lifecycleProvider: LifecycleProvider<E>, event: E? = null): ObservableSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
inline fun <T, E> Observable<T>.autoDispose(lifecycleProvider: LifecycleProvider<E>, event: E? = null): ObservableSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(lifecycleProvider, event)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T, E> Single<T>.autoDisposable(lifecycleProvider: LifecycleProvider<E>, event: E? = null): SingleSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
inline fun <T, E> Single<T>.autoDispose(lifecycleProvider: LifecycleProvider<E>, event: E? = null): SingleSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(lifecycleProvider, event)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T, E> Maybe<T>.autoDisposable(lifecycleProvider: LifecycleProvider<E>, event: E? = null): MaybeSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
inline fun <T, E> Maybe<T>.autoDispose(lifecycleProvider: LifecycleProvider<E>, event: E? = null): MaybeSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(lifecycleProvider, event)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <E> Completable.autoDisposable(lifecycleProvider: LifecycleProvider<E>, event: E? = null): CompletableSubscribeProxy {
  return this.`as`(AutoDispose.autoDisposable<Any>(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
inline fun <E> Completable.autoDispose(lifecycleProvider: LifecycleProvider<E>, event: E? = null): CompletableSubscribeProxy {
  return this.`as`(AutoDispose.autoDisposable<Any>(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
@Deprecated(
    replaceWith = ReplaceWith("autoDispose(lifecycleProvider, event)"),
    message = "Renamed to `autoDispose`",
    level = DeprecationLevel.ERROR
)
inline fun <T, E> ParallelFlowable<T>.autoDisposable(lifecycleProvider: LifecycleProvider<E>, event: E? = null): ParallelFlowableSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable] and takes an [event] when
 * subscription will be disposed.
 *
 * @param lifecycleProvider The lifecycle provider from RxLifecycle.
 * @param event Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
inline fun <T, E> ParallelFlowable<T>.autoDispose(lifecycleProvider: LifecycleProvider<E>, event: E? = null): ParallelFlowableSubscribeProxy<T> {
  return this.`as`(AutoDispose.autoDisposable(lifecycleProvider.scope(event)))
}
