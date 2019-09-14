/*
 * Copyright (C) 2019. Uber Technologies
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
@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.uber.autodispose.coroutinesinterop

import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.autoDispose
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
inline fun <T> Flowable<T>.autoDispose(scope: CoroutineScope): FlowableSubscribeProxy<T> {
  return autoDispose(scope.asScopeProvider())
}

/** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
inline fun <T> Observable<T>.autoDispose(scope: CoroutineScope): ObservableSubscribeProxy<T> {
  return autoDispose(scope.asScopeProvider())
}

/** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
inline fun <T> Single<T>.autoDispose(scope: CoroutineScope): SingleSubscribeProxy<T> {
  return autoDispose(scope.asScopeProvider())
}

/** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
inline fun <T> Maybe<T>.autoDispose(scope: CoroutineScope): MaybeSubscribeProxy<T> {
  return autoDispose(scope.asScopeProvider())
}

/** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
inline fun Completable.autoDispose(scope: CoroutineScope): CompletableSubscribeProxy {
  return autoDispose(scope.asScopeProvider())
}

/**
 * @return a [ScopeProvider] representation of this [CoroutineScope]. This scope will complete when
 *         [this] coroutine scope completes.
 */
fun CoroutineScope.asScopeProvider(): ScopeProvider = ScopeProvider { asUndeferredCompletable() }

/**
 * @return a [Completable] representation of this [CoroutineScope]. This will complete when [this]
 *         coroutine scope completes. Note that the returned [Completable] is deferred.
 */
fun CoroutineScope.asCompletable(): Completable {
  return Completable.defer { asUndeferredCompletable() }
}

private fun CoroutineScope.asUndeferredCompletable(): Completable {
  return Completable.create { emitter ->
    val job = coroutineContext[Job] ?: error(
        "Scope cannot be created because it does not have a job: ${this@asUndeferredCompletable}")
    job.invokeOnCompletion {
      when (it) {
        null, is CancellationException -> emitter.onComplete()
        else -> emitter.onError(it)
      }
    }
  }
}

/**
 * @param context an optional [CoroutineContext] to use for this scope. Default is a new
 *                [SupervisorJob].
 * @return a [CoroutineScope] representation of this [ScopeProvider]. This scope will cancel when
 *         [this] scope provider completes.
 */
fun ScopeProvider.asCoroutineScope(context: CoroutineContext = SupervisorJob()): CoroutineScope {
  return requestScope().asCoroutineScope(context)
}

/**
 * @param context an optional [CoroutineContext] to use for this scope. Default is a new
 *                [SupervisorJob].
 * @return a [CoroutineScope] representation of this [CompletableSource]. This scope will cancel
 *         when [this] scope provider completes.
 */
fun CompletableSource.asCoroutineScope(context: CoroutineContext = SupervisorJob()): CoroutineScope {
  val scope = CoroutineScope(context)

  // Bind to the scope, so if the scope is manually canceled before our scope provider emits, we
  // clean up here.
  Completable.wrap(this)
      .autoDispose(scope)
      .subscribe({ scope.cancel() }) { e -> scope.cancel("OnError", e) }

  return scope
}

/**
 * @param context an optional [CoroutineContext] to use for this scope. Default is a new
 *                [SupervisorJob].
 * @param body a body with [AutoDisposeCoroutineScope] available.
 * @return a [CoroutineScope] representation of this [CompletableSource]. This scope will cancel
 *         when [this] scope provider completes.
 */
fun ScopeProvider.asCoroutineScope(
    context: CoroutineContext = SupervisorJob(),
    body: AutoDisposeCoroutineScope.() -> Unit
) {
  requestScope().asCoroutineScope(context, body)
}

/**
 * @param context an optional [CoroutineContext] to use for this scope. Default is a new
 *                [SupervisorJob].
 * @param body a body with [AutoDisposeCoroutineScope] available.
 * @return a [CoroutineScope] representation of this [CompletableSource]. This scope will cancel
 *         when [this] scope provider completes.
 */
fun CompletableSource.asCoroutineScope(
    context: CoroutineContext = SupervisorJob(),
    body: AutoDisposeCoroutineScope.() -> Unit
) {
  val scope = asCoroutineScope(context)
  RealAutoDisposeCoroutineScope(scope).body()
}

internal class RealAutoDisposeCoroutineScope(private val scope: CoroutineScope) : AutoDisposeCoroutineScope {
  override fun <T> Flowable<T>.autoDispose(): FlowableSubscribeProxy<T> {
    return autoDispose(scope.asScopeProvider())
  }

  override fun <T> Observable<T>.autoDispose(): ObservableSubscribeProxy<T> {
    return autoDispose(scope.asScopeProvider())
  }

  override fun <T> Single<T>.autoDispose(): SingleSubscribeProxy<T> {
    return autoDispose(scope.asScopeProvider())
  }

  override fun <T> Maybe<T>.autoDispose(): MaybeSubscribeProxy<T> {
    return autoDispose(scope.asScopeProvider())
  }

  override fun Completable.autoDispose(): CompletableSubscribeProxy {
    return autoDispose(scope.asScopeProvider())
  }
}

interface AutoDisposeCoroutineScope {
  /** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
  fun <T> Flowable<T>.autoDispose(): FlowableSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
  fun <T> Observable<T>.autoDispose(): ObservableSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
  fun <T> Single<T>.autoDispose(): SingleSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
  fun <T> Maybe<T>.autoDispose(): MaybeSubscribeProxy<T>

  /** Extension that proxies to the normal [autoDispose] extension function with a [ScopeProvider]. */
  fun Completable.autoDispose(): CompletableSubscribeProxy
}
