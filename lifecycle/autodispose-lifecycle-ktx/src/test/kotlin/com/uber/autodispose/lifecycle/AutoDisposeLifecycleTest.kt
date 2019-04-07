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

import io.reactivex.BackpressureStrategy.ERROR
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class AutoDisposeLifecycleTest {
  companion object {
    private const val DEFAULT_PARALLELISM = 2
  }

  private val o = TestObserver<String>()
  private val s = TestSubscriber<String>()
  private val lifecycleScopeProvider = TestLifecycleScopeProvider.create()

  @Test fun observable_lifecycleNotStarted() {
    Observable.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertError { it is LifecycleNotStartedException }
  }

  @Test fun observable_lifecycleNormalCompletion() {
    lifecycleScopeProvider.start()
    Observable.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertValue { it == "Hello" }
    o.assertComplete()
  }

  @Test fun observable_lifecycleNormalInterrupted() {
    lifecycleScopeProvider.start()
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    subject.onNext("Hello")

    o.assertValue { it == "Hello" }

    lifecycleScopeProvider.stop()

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun observable_lifecycleEnded() {
    lifecycleScopeProvider.start()
    lifecycleScopeProvider.stop()
    Observable.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertError { it is LifecycleEndedException }
  }

  @Test fun flowable_lifecycleNotStarted() {
    Flowable.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(s)

    s.assertError { it is LifecycleNotStartedException }
  }

  @Test fun flowable_lifecycleNormalCompletion() {
    lifecycleScopeProvider.start()
    Flowable.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(s)

    s.assertValue { it == "Hello" }
    s.assertComplete()
  }

  @Test fun flowable_lifecycleNormalInterrupted() {
    lifecycleScopeProvider.start()
    val subject = PublishSubject.create<String>()
    subject.toFlowable(ERROR)
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(s)

    subject.onNext("Hello")

    s.assertValue { it == "Hello" }

    lifecycleScopeProvider.stop()

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(s.isDisposed).isTrue()
//    s.assertNotSubscribed()
  }

  @Test fun flowable_lifecycleEnded() {
    lifecycleScopeProvider.start()
    lifecycleScopeProvider.stop()
    Flowable.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(s)

    s.assertError { it is LifecycleEndedException }
  }

  @Test fun maybe_lifecycleNotStarted() {
    Maybe.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertError { it is LifecycleNotStartedException }
  }

  @Test fun maybe_lifecycleNormalCompletion() {
    lifecycleScopeProvider.start()
    Maybe.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertValue { it == "Hello" }
    o.assertComplete()
  }

  @Test fun maybe_lifecycleNormalInterrupted() {
    lifecycleScopeProvider.start()
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    lifecycleScopeProvider.stop()

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun maybe_lifecycleEnded() {
    lifecycleScopeProvider.start()
    lifecycleScopeProvider.stop()
    Maybe.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertError { it is LifecycleEndedException }
  }

  @Test fun single_lifecycleNotStarted() {
    Single.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertError { it is LifecycleNotStartedException }
  }

  @Test fun single_lifecycleNormalCompletion() {
    lifecycleScopeProvider.start()
    Single.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertValue { it == "Hello" }
    o.assertComplete()
  }

  @Test fun single_lifecycleNormalInterrupted() {
    lifecycleScopeProvider.start()
    val subject = PublishSubject.create<String>()
    subject
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    lifecycleScopeProvider.stop()

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun single_lifecycleEnded() {
    lifecycleScopeProvider.start()
    lifecycleScopeProvider.stop()
    Single.just("Hello")
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertError { it is LifecycleEndedException }
  }

  @Test fun completable_lifecycleNotStarted() {
    Completable.complete()
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertError { it is LifecycleNotStartedException }
  }

  @Test fun completable_lifecycleNormalCompletion() {
    lifecycleScopeProvider.start()
    Completable.complete()
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertComplete()
  }

  @Test fun completable_lifecycleNormalInterrupted() {
    lifecycleScopeProvider.start()
    val subject = CompletableSubject.create()
    subject
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    lifecycleScopeProvider.stop()

    // https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(o.isDisposed).isTrue()
//    o.assertNotSubscribed()
  }

  @Test fun completable_lifecycleEnded() {
    lifecycleScopeProvider.start()
    lifecycleScopeProvider.stop()
    Completable.complete()
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(o)

    o.assertError { it is LifecycleEndedException }
  }

  @Test fun parallelFlowable_lifecycleNotStarted() {
    val s2 = TestSubscriber<String>()
    Flowable.just("Hello", "World")
        .parallel(DEFAULT_PARALLELISM)
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(arrayOf(s, s2))

    s.assertError { it is LifecycleNotStartedException }
    s2.assertError { it is LifecycleNotStartedException }
  }

  @Test fun parallelFlowable_lifecycleNormalCompletion() {
    lifecycleScopeProvider.start()
    val s2 = TestSubscriber<String>()
    Flowable.just("Hello", "World")
        .parallel(DEFAULT_PARALLELISM)
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(arrayOf(s, s2))

    s.assertValue { it == "Hello" }
    s.assertComplete()
    s2.assertValue { it == "World" }
    s2.assertComplete()
  }

  @Test fun parallelFlowable_lifecycleNormalInterrupted() {
    lifecycleScopeProvider.start()
    val subject = PublishSubject.create<String>()
    val s2 = TestSubscriber<String>()
    subject.toFlowable(ERROR)
        .parallel(DEFAULT_PARALLELISM)
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(arrayOf(s, s2))

    subject.onNext("Hello")
    subject.onNext("World")

    s.assertValue { it == "Hello" }
    s2.assertValue { it == "World" }
    lifecycleScopeProvider.stop()

// https://github.com/ReactiveX/RxJava/issues/5178
//    assertThat(s.isDisposed).isTrue()
//    s.assertNotSubscribed()
  }

  @Test fun parallelFlowable_lifecycleEnded() {
    lifecycleScopeProvider.start()
    lifecycleScopeProvider.stop()
    val s2 = TestSubscriber<String>()
    Flowable.just("Hello")
        .parallel(DEFAULT_PARALLELISM)
        .autoDisposable(lifecycleScopeProvider)
        .subscribe(arrayOf(s, s2))

    s.assertError { it is LifecycleEndedException }
    s2.assertError { it is LifecycleEndedException }
  }
}
