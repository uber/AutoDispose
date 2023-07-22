/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("NOTHING_TO_INLINE")

package autodispose2.androidx.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleOwner
import autodispose2.AutoDispose
import autodispose2.CompletableSubscribeProxy
import autodispose2.FlowableSubscribeProxy
import autodispose2.MaybeSubscribeProxy
import autodispose2.ObservableSubscribeProxy
import autodispose2.ParallelFlowableSubscribeProxy
import autodispose2.ScopeProvider
import autodispose2.SingleSubscribeProxy
import autodispose2.lifecycle.CorrespondingEventsFunction
import io.reactivex.rxjava3.annotations.CheckReturnValue
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.parallel.ParallelFlowable

/** Extension that returns a [ScopeProvider] for this [LifecycleOwner]. */
@CheckReturnValue
public inline fun LifecycleOwner.scope(): ScopeProvider = AndroidLifecycleScopeProvider.from(this)

/**
 * Extension that returns a [ScopeProvider] for this [LifecycleOwner].
 *
 * @param untilEvent the event until the scope is valid.
 */
@CheckReturnValue
public inline fun LifecycleOwner.scope(untilEvent: Event): ScopeProvider =
  AndroidLifecycleScopeProvider.from(this, untilEvent)

/**
 * Extension that returns a [ScopeProvider] for this [LifecycleOwner].
 *
 * @param boundaryResolver function that resolves the event boundary.
 */
@CheckReturnValue
public inline fun LifecycleOwner.scope(
  boundaryResolver: CorrespondingEventsFunction<Event>
): ScopeProvider = AndroidLifecycleScopeProvider.from(this, boundaryResolver)

/** Extension that returns a [ScopeProvider] for this [Lifecycle]. */
@CheckReturnValue
public inline fun Lifecycle.scope(): ScopeProvider = AndroidLifecycleScopeProvider.from(this)

/**
 * Extension that returns a [ScopeProvider] for this [Lifecycle].
 *
 * @param untilEvent the event until the scope is valid.
 */
@CheckReturnValue
public inline fun Lifecycle.scope(untilEvent: Event): ScopeProvider =
  AndroidLifecycleScopeProvider.from(this, untilEvent)

/**
 * Extension that returns a [ScopeProvider] for this [Lifecycle].
 *
 * @param boundaryResolver function that resolves the event boundary.
 */
@CheckReturnValue
public inline fun Lifecycle.scope(
  boundaryResolver: CorrespondingEventsFunction<Event>
): ScopeProvider = AndroidLifecycleScopeProvider.from(this, boundaryResolver)

/**
 * Extension that proxies to [Flowable.as] + [AutoDispose.autoDisposable] and takes an [untilEvent]
 * when subscription will be disposed.
 *
 * @param lifecycleOwner The lifecycle owner.
 * @param untilEvent Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
public inline fun <T : Any> Flowable<T>.autoDispose(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event? = null
): FlowableSubscribeProxy<T> {
  return if (untilEvent == null) {
    this.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
  } else {
    this.to(
      AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent))
    )
  }
}

/**
 * Extension that proxies to [Observable.as] + [AutoDispose.autoDisposable] and takes an
 * [untilEvent] when subscription will be disposed.
 *
 * @param lifecycleOwner The lifecycle owner.
 * @param untilEvent Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
public inline fun <T : Any> Observable<T>.autoDispose(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event? = null
): ObservableSubscribeProxy<T> {
  return if (untilEvent == null) {
    this.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
  } else {
    this.to(
      AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent))
    )
  }
}

/**
 * Extension that proxies to [Single.as] + [AutoDispose.autoDisposable] and takes an [untilEvent]
 * when subscription will be disposed.
 *
 * @param lifecycleOwner The lifecycle owner.
 * @param untilEvent Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
public inline fun <T : Any> Single<T>.autoDispose(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event? = null
): SingleSubscribeProxy<T> {
  return if (untilEvent == null) {
    this.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
  } else {
    this.to(
      AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent))
    )
  }
}

/**
 * Extension that proxies to [Maybe.as] + [AutoDispose.autoDisposable] and takes an [untilEvent]
 * when subscription will be disposed.
 *
 * @param lifecycleOwner The lifecycle owner.
 * @param untilEvent Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
public inline fun <T : Any> Maybe<T>.autoDispose(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event? = null
): MaybeSubscribeProxy<T> {
  return if (untilEvent == null) {
    this.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
  } else {
    this.to(
      AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent))
    )
  }
}

/**
 * Extension that proxies to [Completable.as] + [AutoDispose.autoDisposable] and takes an
 * [untilEvent] when subscription will be disposed.
 *
 * @param lifecycleOwner The lifecycle owner.
 * @param untilEvent Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
public inline fun Completable.autoDispose(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event? = null
): CompletableSubscribeProxy {
  return if (untilEvent == null) {
    this.to(AutoDispose.autoDisposable<Any>(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
  } else {
    this.to(
      AutoDispose.autoDisposable<Any>(
        AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent)
      )
    )
  }
}

/**
 * Extension that proxies to [ParallelFlowable.as] + [AutoDispose.autoDisposable] and takes an
 * [untilEvent] when subscription will be disposed.
 *
 * @param lifecycleOwner The lifecycle owner.
 * @param untilEvent Optional lifecycle event when subscription will be disposed.
 */
@CheckReturnValue
public inline fun <T : Any> ParallelFlowable<T>.autoDispose(
  lifecycleOwner: LifecycleOwner,
  untilEvent: Event? = null
): ParallelFlowableSubscribeProxy<T> {
  return if (untilEvent == null) {
    this.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
  } else {
    this.to(
      AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner, untilEvent))
    )
  }
}
