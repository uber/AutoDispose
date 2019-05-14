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
package com.uber.autodispose.android.lifecycle

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.uber.autodispose.OutsideScopeException
import com.uber.autodispose.android.lifecycle.test.TestLifecycleOwner
import com.uber.autodispose.test.RecordingObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinExtensionsTest {

  companion object {
    private val LOGGER = RecordingObserver.Logger {
      message -> Log.d(KotlinExtensionsTest::class.java.simpleName, message) }
  }

  @Test
  @UiThreadTest
  fun observable_beforeCreate() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    // Spin it up
    val lifecycle = TestLifecycleOwner.create()
    subject.autoDisposable(lifecycle).subscribe(o)
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)

    val d = o.takeSubscribe()
    o.assertNoMoreEvents() // No initial value.

    subject.onNext(0)
    assertThat(o.takeNext()).isEqualTo(0)

    subject.onNext(1)
    assertThat(o.takeNext()).isEqualTo(1)

    lifecycle.emit(Lifecycle.Event.ON_PAUSE)
    lifecycle.emit(Lifecycle.Event.ON_STOP)
    lifecycle.emit(Lifecycle.Event.ON_DESTROY)

    subject.onNext(2)
    o.assertNoMoreEvents()
  }

  @Test
  @UiThreadTest
  fun observable_createDestroy() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    // Spin it up
    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    subject.autoDisposable(lifecycle).subscribe(o)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)

    val d = o.takeSubscribe()
    o.assertNoMoreEvents() // No initial value.

    subject.onNext(0)
    assertThat(o.takeNext()).isEqualTo(0)

    subject.onNext(1)
    assertThat(o.takeNext()).isEqualTo(1)

    lifecycle.emit(Lifecycle.Event.ON_PAUSE)
    lifecycle.emit(Lifecycle.Event.ON_STOP)
    lifecycle.emit(Lifecycle.Event.ON_DESTROY)

    subject.onNext(2)
    o.assertNoMoreEvents()
  }

  @Test
  @UiThreadTest
  fun observable_startStop() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    // Spin it up
    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    lifecycle.emit(Lifecycle.Event.ON_START)
    subject.autoDisposable(lifecycle).subscribe(o)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)

    val d = o.takeSubscribe()
    o.assertNoMoreEvents() // No initial value.

    subject.onNext(0)
    assertThat(o.takeNext()).isEqualTo(0)

    subject.onNext(1)
    assertThat(o.takeNext()).isEqualTo(1)

    lifecycle.emit(Lifecycle.Event.ON_PAUSE)

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_STOP)
    subject.onNext(2)
    o.assertNoMoreEvents()

    lifecycle.emit(Lifecycle.Event.ON_DESTROY)
  }

  @Test
  @UiThreadTest
  fun observable_resumePause() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    // Spin it up
    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)
    subject.autoDisposable(lifecycle).subscribe(o)

    val d = o.takeSubscribe()
    o.assertNoMoreEvents() // No initial value.

    subject.onNext(0)
    assertThat(o.takeNext()).isEqualTo(0)

    subject.onNext(1)
    assertThat(o.takeNext()).isEqualTo(1)

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_PAUSE)
    subject.onNext(2)
    o.assertNoMoreEvents()

    lifecycle.emit(Lifecycle.Event.ON_STOP)
    lifecycle.emit(Lifecycle.Event.ON_DESTROY)
  }

  @Test
  @UiThreadTest
  fun observable_createPause() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    // Spin it up
    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    subject
        .autoDisposable(lifecycle, Lifecycle.Event.ON_PAUSE)
        .subscribe(o)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)

    val d = o.takeSubscribe()
    o.assertNoMoreEvents() // No initial value.

    subject.onNext(0)
    assertThat(o.takeNext()).isEqualTo(0)

    subject.onNext(1)
    assertThat(o.takeNext()).isEqualTo(1)

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_PAUSE)
    subject.onNext(2)
    o.assertNoMoreEvents()

    lifecycle.emit(Lifecycle.Event.ON_STOP)
    lifecycle.emit(Lifecycle.Event.ON_DESTROY)
  }

  @Test
  @UiThreadTest
  fun observable_resumeDestroy() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    // Spin it up
    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)
    subject
        .autoDisposable(lifecycle, Lifecycle.Event.ON_DESTROY)
        .subscribe(o)

    val d = o.takeSubscribe()
    o.assertNoMoreEvents() // No initial value.

    subject.onNext(0)
    assertThat(o.takeNext()).isEqualTo(0)

    subject.onNext(1)
    assertThat(o.takeNext()).isEqualTo(1)

    lifecycle.emit(Lifecycle.Event.ON_PAUSE)
    lifecycle.emit(Lifecycle.Event.ON_STOP)

    subject.onNext(2)
    assertThat(o.takeNext()).isEqualTo(2)

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_DESTROY)
    subject.onNext(3)
    o.assertNoMoreEvents()
  }

  @Test
  @UiThreadTest
  fun observable_offAfterPause_shouldStopOnStop() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)
    lifecycle.emit(Lifecycle.Event.ON_PAUSE)
    subject.autoDisposable(lifecycle).subscribe(o)

    val d = o.takeSubscribe()

    subject.onNext(2)
    assertThat(o.takeNext()).isEqualTo(2)

    // We could resume again
    lifecycle.emit(Lifecycle.Event.ON_RESUME)
    subject.onNext(3)
    assertThat(o.takeNext()).isEqualTo(3)

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_STOP)
    subject.onNext(3)
    o.assertNoMoreEvents()
  }

  @Test
  @UiThreadTest
  fun observable_offAfterOnDestroyView_shouldStopOnDestroy() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)
    lifecycle.emit(Lifecycle.Event.ON_PAUSE)
    lifecycle.emit(Lifecycle.Event.ON_STOP)
    // In a CREATED state now but the next event will be destroy
    // This simulates subscribing in fragments' onDestroyView, where we want the subscription to
    // still dispose properly in onDestroy.
    subject.autoDisposable(lifecycle).subscribe(o)

    val d = o.takeSubscribe()

    subject.onNext(2)
    assertThat(o.takeNext()).isEqualTo(2)

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_DESTROY)
    subject.onNext(3)
    o.assertNoMoreEvents()
  }

  @Test
  fun observable_offMainThread_shouldFail() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    // Spin it up
    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)
    subject.autoDisposable(lifecycle).subscribe(o)

    val d = o.takeSubscribe()
    val t = o.takeError()
    assertThat(t).isInstanceOf(IllegalStateException::class.java)
    assertThat(t.message).contains("main thread")
    o.assertNoMoreEvents()
    assertThat(d.isDisposed).isTrue()
  }

  @Test(expected = OutsideScopeException::class)
  @UiThreadTest
  fun observable_offAfterDestroy_shouldFail() {
    val o = RecordingObserver<Int>(LOGGER)
    val subject = PublishSubject.create<Int>()

    val lifecycle = TestLifecycleOwner.create()
    lifecycle.emit(Lifecycle.Event.ON_CREATE)
    lifecycle.emit(Lifecycle.Event.ON_START)
    lifecycle.emit(Lifecycle.Event.ON_RESUME)
    lifecycle.emit(Lifecycle.Event.ON_PAUSE)
    lifecycle.emit(Lifecycle.Event.ON_STOP)
    lifecycle.emit(Lifecycle.Event.ON_DESTROY)

    subject.autoDisposable(lifecycle).subscribe(o)
  }
}
