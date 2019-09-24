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
package autodispose2.interop.coroutines

import autodispose2.TestScopeProvider
import autodispose2.test.RecordingObserver
import com.google.common.truth.Truth.assertThat
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.subjects.CompletableSubject
import io.reactivex.rxjava3.subjects.MaybeSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.SingleSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import org.junit.Test

class AutoDisposeCoroutinesInteropTest {

  @Test
  fun flowable() {
    val job = Job()
    val scope = CoroutineScope(job)
    val source = PublishProcessor.create<Int>()
    val o = source.autoDispose(scope).test()
    assertThat(o.hasSubscription()).isTrue()

    assertThat(source.hasSubscribers()).isTrue()
    scope.ensureActive()

    source.onNext(1)
    o.assertValue(1)

    source.onNext(2)

    assertThat(source.hasSubscribers()).isTrue()
    scope.ensureActive()
    o.assertValues(1, 2)

    scope.cancel()
    source.onNext(3)

    // Nothing new
    o.assertValues(1, 2)

    // Unsubscribed
    assertThat(source.hasSubscribers()).isFalse()
    assertThat(scope.isActive).isFalse()
  }

  @Test
  fun observable() {
    val job = Job()
    val scope = CoroutineScope(job)
    val o = RecordingObserver<Int>(LOGGER)
    val source = PublishSubject.create<Int>()
    source.autoDispose(scope).subscribe(o)
    o.takeSubscribe()

    assertThat(source.hasObservers()).isTrue()
    scope.ensureActive()

    source.onNext(1)
    assertThat(o.takeNext()).isEqualTo(1)

    source.onNext(2)

    assertThat(source.hasObservers()).isTrue()
    scope.ensureActive()
    assertThat(o.takeNext()).isEqualTo(2)

    scope.cancel()
    source.onNext(3)

    o.assertNoMoreEvents()
    assertThat(source.hasObservers()).isFalse()
    assertThat(scope.isActive).isFalse()
  }

  @Test
  fun maybe() {
    val job = Job()
    val scope = CoroutineScope(job)
    val o = RecordingObserver<Int>(LOGGER)
    val source = MaybeSubject.create<Int>()
    source.autoDispose(scope).subscribe(o)
    o.takeSubscribe()

    assertThat(source.hasObservers()).isTrue()
    scope.ensureActive()

    scope.cancel()

    // All disposed
    assertThat(source.hasObservers()).isFalse()
    assertThat(scope.isActive).isFalse()

    // No one is listening
    source.onSuccess(3)
    o.assertNoMoreEvents()
  }

  @Test
  fun single() {
    val job = Job()
    val scope = CoroutineScope(job)
    val o = RecordingObserver<Int>(LOGGER)
    val source = SingleSubject.create<Int>()
    source.autoDispose(scope).subscribe(o)
    o.takeSubscribe()

    assertThat(source.hasObservers()).isTrue()
    scope.ensureActive()

    scope.cancel()

    // All disposed
    assertThat(source.hasObservers()).isFalse()
    assertThat(scope.isActive).isFalse()

    // No one is listening
    source.onSuccess(3)
    o.assertNoMoreEvents()
  }

  @Test
  fun completable() {
    val job = Job()
    val scope = CoroutineScope(job)
    val o = RecordingObserver<Any>(LOGGER)
    val source = CompletableSubject.create()
    source.autoDispose(scope).subscribe(o)
    o.takeSubscribe()

    assertThat(source.hasObservers()).isTrue()
    scope.ensureActive()

    scope.cancel()

    // All disposed
    assertThat(source.hasObservers()).isFalse()
    assertThat(scope.isActive).isFalse()

    // No one is listening
    source.onComplete()
    o.assertNoMoreEvents()
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
    providerObserver.assertNoErrors().assertNotComplete()
    scope.cancel()
    providerObserver.assertComplete()
  }

  @Test
  fun scopeToProviderError() {
    val job = Job()
    val scope = CoroutineScope(job)
    val provider = scope.asScopeProvider()
    val providerObserver = Completable.wrap(provider.requestScope()).test()
    providerObserver.assertNoErrors().assertNotComplete()
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
    observer.assertNoErrors().assertNotComplete()
    scope.cancel()
    observer.assertComplete()
  }

  @Test
  fun scopeToCompletableError() {
    val job = Job()
    val scope = CoroutineScope(job)
    val completable = scope.asCompletable()
    val observer = completable.test()
    observer.assertNoErrors().assertNotComplete()
    val error = RuntimeException()
    scope.cancel("OnError", error)
    observer.assertComplete()
  }

  companion object {
    private val LOGGER = { message: String ->
      println(AutoDisposeCoroutinesInteropTest::class.java.simpleName + ": " + message)
    }
  }
}
