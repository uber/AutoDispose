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

package com.uber.autodispose.kotlin

import com.uber.autodispose.LifecycleEndedException
import com.uber.autodispose.LifecycleNotStartedException
import com.uber.autodispose.LifecycleScopeProvider
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.kotlin.LifecycleEvent.End
import com.uber.autodispose.kotlin.LifecycleEvent.Start
import io.reactivex.BackpressureStrategy.ERROR
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

sealed class LifecycleEvent {
  class Start: LifecycleEvent()
  class End: LifecycleEvent()
}

class AutoDisposeKotlinTest {

  val o = TestObserver<String>()
  val s = TestSubscriber<String>()
  val scopeMaybe = MaybeSubject.create<Any>()
  val scopeProvider = ScopeProvider { scopeMaybe }
  val lifecycleEvents = BehaviorSubject.create<LifecycleEvent>()
  val lifecycle = object : LifecycleScopeProvider<LifecycleEvent> {
    override fun lifecycle(): Observable<LifecycleEvent> {
      return lifecycleEvents
    }

    override fun peekLifecycle(): LifecycleEvent? {
      return lifecycleEvents.value
    }

    override fun correspondingEvents(): Function<LifecycleEvent, LifecycleEvent> {
      return Function { event ->
        when (event) {
          is Start -> End()
          is End -> throw LifecycleEndedException()
        }
      }
    }
  }

  @Test fun observable_maybeNormalCompletion() {
    Observable.just("Hello")
        .autoDisposeWith(scopeMaybe)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun observable_maybeNormalInterrupted() {
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposeWith(scopeMaybe)
        .subscribe(o)

    subject.onNext("Hello")

    o.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun observable_scopeProviderNormalCompletion() {
    Observable.just("Hello")
        .autoDisposeWith(scopeProvider)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun observable_scopeProviderNormalInterrupted() {
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposeWith(scopeProvider)
        .subscribe(o)

    subject.onNext("Hello")

    o.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun observable_lifecycleNotStarted() {
    Observable.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertError { t -> t is LifecycleNotStartedException }
  }

  @Test fun observable_lifecycleNormalCompletion() {
    lifecycleEvents.onNext(Start())
    Observable.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun observable_lifecycleNormalInterrupted() {
    lifecycleEvents.onNext(Start())
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    subject.onNext("Hello")

    o.assertValue { v -> v == "Hello" }

    lifecycleEvents.onNext(End())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun observable_lifecycleEnded() {
    lifecycleEvents.onNext(Start())
    lifecycleEvents.onNext(End())
    Observable.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertError { t -> t is LifecycleEndedException }
  }

  @Test fun flowable_maybeNormalCompletion() {
    Flowable.just("Hello")
        .autoDisposeWith(scopeMaybe)
        .subscribe(s)

    s.assertValue { v -> v == "Hello" }
    s.assertComplete()
  }

  @Test fun flowable_maybeNormalInterrupted() {
    val subject = PublishSubject.create<String>()
    subject.toFlowable(ERROR)
        .autoDisposeWith(scopeMaybe)
        .subscribe(s)

    subject.onNext("Hello")

    s.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(s.isDisposed).isTrue()
//    s.assertNotSubscribed()
  }

  @Test fun flowable_scopeProviderNormalCompletion() {
    Flowable.just("Hello")
        .autoDisposeWith(scopeProvider)
        .subscribe(s)

    s.assertValue { v -> v == "Hello" }
    s.assertComplete()
  }

  @Test fun flowable_scopeProviderNormalInterrupted() {
    val subject = PublishSubject.create<String>()
    subject.toFlowable(ERROR)
        .autoDisposeWith(scopeProvider)
        .subscribe(s)

    subject.onNext("Hello")

    s.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(s.isDisposed).isTrue()
//    s.assertNotSubscribed()
  }

  @Test fun flowable_lifecycleNotStarted() {
    Flowable.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(s)

    s.assertError { t -> t is LifecycleNotStartedException }
  }

  @Test fun flowable_lifecycleNormalCompletion() {
    lifecycleEvents.onNext(Start())
    Flowable.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(s)

    s.assertValue { v -> v == "Hello" }
    s.assertComplete()
  }

  @Test fun flowable_lifecycleNormalInterrupted() {
    lifecycleEvents.onNext(Start())
    val subject = PublishSubject.create<String>()
    subject.toFlowable(ERROR)
        .autoDisposeWith(lifecycle)
        .subscribe(s)

    subject.onNext("Hello")

    s.assertValue { v -> v == "Hello" }

    lifecycleEvents.onNext(End())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(s.isDisposed).isTrue()
//    s.assertNotSubscribed()
  }

  @Test fun flowable_lifecycleEnded() {
    lifecycleEvents.onNext(Start())
    lifecycleEvents.onNext(End())
    Flowable.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(s)

    s.assertError { t -> t is LifecycleEndedException }
  }

  @Test fun maybe_maybeNormalCompletion() {
    Maybe.just("Hello")
        .autoDisposeWith(scopeMaybe)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun maybe_maybeNormalInterrupted() {
    val subject = MaybeSubject.create<String>()
    subject
        .autoDisposeWith(scopeMaybe)
        .subscribe(o)

    subject.onSuccess("Hello")

    o.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun maybe_scopeProviderNormalCompletion() {
    Maybe.just("Hello")
        .autoDisposeWith(scopeProvider)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun maybe_scopeProviderNormalInterrupted() {
    val subject = MaybeSubject.create<String>()
    subject
        .autoDisposeWith(scopeProvider)
        .subscribe(o)

    subject.onSuccess("Hello")

    o.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun maybe_lifecycleNotStarted() {
    Maybe.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertError { t -> t is LifecycleNotStartedException }
  }

  @Test fun maybe_lifecycleNormalCompletion() {
    lifecycleEvents.onNext(Start())
    Maybe.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun maybe_lifecycleNormalInterrupted() {
    lifecycleEvents.onNext(Start())
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    lifecycleEvents.onNext(End())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun maybe_lifecycleEnded() {
    lifecycleEvents.onNext(Start())
    lifecycleEvents.onNext(End())
    Maybe.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertError { t -> t is LifecycleEndedException }
  }

  @Test fun single_maybeNormalCompletion() {
    Single.just("Hello")
        .autoDisposeWith(scopeMaybe)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun single_maybeNormalInterrupted() {
    val subject = SingleSubject.create<String>()
    subject
        .autoDisposeWith(scopeMaybe)
        .subscribe(o)

    subject.onSuccess("Hello")

    o.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun single_scopeProviderNormalCompletion() {
    Single.just("Hello")
        .autoDisposeWith(scopeProvider)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun single_scopeProviderNormalInterrupted() {
    val subject = SingleSubject.create<String>()
    subject
        .autoDisposeWith(scopeProvider)
        .subscribe(o)

    subject.onSuccess("Hello")

    o.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun single_lifecycleNotStarted() {
    Single.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertError { t -> t is LifecycleNotStartedException }
  }

  @Test fun single_lifecycleNormalCompletion() {
    lifecycleEvents.onNext(Start())
    Single.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertValue { v -> v == "Hello" }
    o.assertComplete()
  }

  @Test fun single_lifecycleNormalInterrupted() {
    lifecycleEvents.onNext(Start())
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    lifecycleEvents.onNext(End())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun single_lifecycleEnded() {
    lifecycleEvents.onNext(Start())
    lifecycleEvents.onNext(End())
    Single.just("Hello")
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertError { t -> t is LifecycleEndedException }
  }

  @Test fun completable_maybeNormalCompletion() {
    Completable.complete()
        .autoDisposeWith(scopeMaybe)
        .subscribe(o)

    o.assertComplete()
  }

  @Test fun completable_maybeNormalInterrupted() {
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposeWith(scopeMaybe)
        .subscribe(o)

    subject.onNext("Hello")

    o.assertValue { v -> v == "Hello" }

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun completable_scopeProviderNormalCompletion() {
    Completable.complete()
        .autoDisposeWith(scopeProvider)
        .subscribe(o)

    o.assertComplete()
  }

  @Test fun completable_scopeProviderNormalInterrupted() {
    val subject = CompletableSubject.create()
    subject
        .autoDisposeWith(scopeProvider)
        .subscribe(o)

    subject.onComplete()

    scopeMaybe.onSuccess(Object())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun completable_lifecycleNotStarted() {
    Completable.complete()
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertError { t -> t is LifecycleNotStartedException }
  }

  @Test fun completable_lifecycleNormalCompletion() {
    lifecycleEvents.onNext(Start())
    Completable.complete()
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertComplete()
  }

  @Test fun completable_lifecycleNormalInterrupted() {
    lifecycleEvents.onNext(Start())
    val subject = CompletableSubject.create()
    subject
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    lifecycleEvents.onNext(End())

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun completable_lifecycleEnded() {
    lifecycleEvents.onNext(Start())
    lifecycleEvents.onNext(End())
    Completable.complete()
        .autoDisposeWith(lifecycle)
        .subscribe(o)

    o.assertError { t -> t is LifecycleEndedException }
  }

}
