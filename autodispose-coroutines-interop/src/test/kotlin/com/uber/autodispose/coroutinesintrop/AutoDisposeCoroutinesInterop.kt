package com.uber.autodispose.coroutinesintrop

import com.google.common.truth.Truth.assertThat
import com.uber.autodispose.TestScopeProvider
import com.uber.autodispose.coroutinesinterop.asCompletable
import com.uber.autodispose.coroutinesinterop.asCoroutineScope
import com.uber.autodispose.coroutinesinterop.asScopeProvider
import com.uber.autodispose.coroutinesinterop.autoDispose
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
    val job = Job()
    val scope = CoroutineScope(job)
    val source = PublishProcessor.create<Int>()
    val o = source.autoDispose(scope).test()
    o.assertSubscribed()

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
