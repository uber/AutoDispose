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
package com.uber.autodispose.coroutinesintrop

import com.google.common.truth.Truth.assertThat
import com.uber.autodispose.TestScopeProvider
import com.uber.autodispose.coroutinesinterop.asCompletable
import com.uber.autodispose.coroutinesinterop.asCoroutineScope
import com.uber.autodispose.coroutinesinterop.asScopeProvider
import com.uber.autodispose.test.RecordingObserver
import io.reactivex.Completable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import org.junit.Test

class AutoDisposeCoroutinesInterop {

  @Test
  fun flowable() {
    val scopeSource = CompletableSubject.create()
    val source = PublishProcessor.create<Int>()
    scopeSource.asCoroutineScope {
      val o = source.autoDispose().test()
      o.assertSubscribed()

      assertThat(source.hasSubscribers()).isTrue()
      assertThat(scopeSource.hasObservers()).isTrue()

      source.onNext(1)
      o.assertValue(1)

      source.onNext(2)

      assertThat(source.hasSubscribers()).isTrue()
      assertThat(scopeSource.hasObservers()).isTrue()
      o.assertValues(1, 2)

      scopeSource.onComplete()
      source.onNext(3)

      // Nothing new
      o.assertValues(1, 2)

      // Unsubscribed
      assertThat(source.hasSubscribers()).isFalse()
      assertThat(scopeSource.hasObservers()).isFalse()
    }
  }

  @Test
  fun observable() {
    val scopeSource = CompletableSubject.create()
    scopeSource.asCoroutineScope {
      val o = RecordingObserver<Int>(LOGGER)
      val source = PublishSubject.create<Int>()
      source.autoDispose().subscribe(o)
      o.takeSubscribe()

      assertThat(source.hasObservers()).isTrue()
      assertThat(scopeSource.hasObservers())

      source.onNext(1)
      assertThat(o.takeNext()).isEqualTo(1)

      source.onNext(2)

      assertThat(source.hasObservers()).isTrue()
      assertThat(scopeSource.hasObservers())
      assertThat(o.takeNext()).isEqualTo(2)

      scopeSource.onComplete()
      source.onNext(3)

      o.assertNoMoreEvents()
      assertThat(source.hasObservers()).isFalse()
      assertThat(scopeSource.hasObservers()).isFalse()
    }
  }

  @Test
  fun maybe() {
    val scopeSource = CompletableSubject.create()
    scopeSource.asCoroutineScope {
      val o = RecordingObserver<Int>(LOGGER)
      val source = MaybeSubject.create<Int>()
      source.autoDispose().subscribe(o)
      o.takeSubscribe()

      assertThat(source.hasObservers()).isTrue()
      assertThat(scopeSource.hasObservers())

      scopeSource.onComplete()

      // All disposed
      assertThat(source.hasObservers()).isFalse()
      assertThat(scopeSource.hasObservers()).isFalse()

      // No one is listening
      source.onSuccess(3)
      o.assertNoMoreEvents()
    }
  }

  @Test
  fun single() {
    val scopeSource = CompletableSubject.create()
    scopeSource.asCoroutineScope {
      val o = RecordingObserver<Int>(LOGGER)
      val source = SingleSubject.create<Int>()
      source.autoDispose().subscribe(o)
      o.takeSubscribe()

      assertThat(source.hasObservers()).isTrue()
      assertThat(scopeSource.hasObservers())

      scopeSource.onComplete()

      // All disposed
      assertThat(source.hasObservers()).isFalse()
      assertThat(scopeSource.hasObservers()).isFalse()

      // No one is listening
      source.onSuccess(3)
      o.assertNoMoreEvents()
    }
  }

  @Test
  fun completable() {
    val scopeSource = CompletableSubject.create()
    scopeSource.asCoroutineScope {
      val o = RecordingObserver<Any>(LOGGER)
      val source = CompletableSubject.create()
      source.autoDispose().subscribe(o)
      o.takeSubscribe()

      assertThat(source.hasObservers()).isTrue()
      assertThat(scopeSource.hasObservers())

      scopeSource.onComplete()

      // All disposed
      assertThat(source.hasObservers()).isFalse()
      assertThat(scopeSource.hasObservers()).isFalse()

      // No one is listening
      source.onComplete()
      o.assertNoMoreEvents()
    }
  }

  @Test
  fun scopeProviderToScope() {
    val provider = TestScopeProvider.create()
    val scope = provider.asCoroutineScope()
    scope.ensureActive()
    provider.emit()
    assertThat(scope.isActive).isFalse()
  }

  @Test
  fun completableToScope() {
    val completableSubject = CompletableSubject.create()
    val scope = completableSubject.asCoroutineScope()
    scope.ensureActive()
    completableSubject.onComplete()
    assertThat(scope.isActive).isFalse()
  }

  @Test
  fun completableToScopeError() {
    val completableSubject = CompletableSubject.create()
    val scope = completableSubject.asCoroutineScope()
    scope.ensureActive()
    val error = RuntimeException()
    completableSubject.onError(error)
    assertThat(scope.isActive).isFalse()
  }

  @Test
  fun scopeToProvider() {
    val job = Job()
    val scope = CoroutineScope(job)
    val provider = scope.asScopeProvider()
    val providerObserver = Completable.wrap(provider.requestScope()).test()
    providerObserver.assertNotTerminated()
    scope.cancel()
    providerObserver.assertComplete()
  }

  @Test
  fun scopeToProviderError() {
    val job = Job()
    val scope = CoroutineScope(job)
    val provider = scope.asScopeProvider()
    val providerObserver = Completable.wrap(provider.requestScope()).test()
    providerObserver.assertNotTerminated()
    val error = RuntimeException()
    scope.cancel("OnError", error)
    providerObserver.assertComplete()
  }

  @Test
  fun scopeToCompletable() {
    val job = Job()
    val scope = CoroutineScope(job)
    val completable = scope.asCompletable()
    val observer = completable.test()
    observer.assertNotTerminated()
    scope.cancel()
    observer.assertComplete()
  }

  @Test
  fun scopeToCompletableError() {
    val job = Job()
    val scope = CoroutineScope(job)
    val completable = scope.asCompletable()
    val observer = completable.test()
    observer.assertNotTerminated()
    val error = RuntimeException()
    scope.cancel("OnError", error)
    observer.assertComplete()
  }

  companion object {
    private val LOGGER = { message: String ->
      println(AutoDisposeCoroutinesInterop::class.java.simpleName + ": " + message)
    }
  }
}
