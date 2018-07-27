/*
 * Copyright (C) 2018. Uber Technologies
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

package com.uber.autodispose.lifecycle.ktx

import com.google.common.truth.Truth.assertThat
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.ktx.KotlinLifecycleScopeProviderTest.LifecycleEvent.START
import com.uber.autodispose.lifecycle.ktx.KotlinLifecycleScopeProviderTest.LifecycleEvent.STOP
import com.uber.autodispose.test.RecordingObserver
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Test

class KotlinLifecycleScopeProviderTest {

  internal enum class LifecycleEvent {
    START, STOP
  }

  /**
   * A thing with a lifecycle that uses the default implementation of [requestScope].
   */
  internal class ThingWithALifecycle : KotlinLifecycleScopeProvider<LifecycleEvent> {

    var lifecycle: BehaviorSubject<LifecycleEvent> = BehaviorSubject.createDefault(
        START)

    override fun lifecycle(): Observable<LifecycleEvent> {
      return lifecycle.hide()
    }

    override fun correspondingEvents(): CorrespondingEventsFunction<LifecycleEvent> {
      return CorrespondingEventsFunction { event ->
        when (event) {
          START -> STOP
          STOP -> throw LifecycleEndedException(
              "Ended!")
        }
      }
    }

    override fun peekLifecycle(): LifecycleEvent {
      return lifecycle.value
    }
  }

  @Test
  fun smokeTest() {
    val o = RecordingObserver<Int>(System.out::println)
    val source = PublishSubject.create<Int>()
    val provider = ThingWithALifecycle()
    val lifecycle = provider.lifecycle
    source.autoDisposable(provider)
        .subscribe(o)
    o.takeSubscribe()

    assertThat(source.hasObservers()).isTrue()
    assertThat(lifecycle.hasObservers()).isTrue()

    source.onNext(1)
    assertThat(o.takeNext()).isEqualTo(1)

    lifecycle.onNext(START)
    source.onNext(2)

    assertThat(source.hasObservers()).isTrue()
    assertThat(lifecycle.hasObservers()).isTrue()
    assertThat(o.takeNext()).isEqualTo(2)

    lifecycle.onNext(STOP)
    source.onNext(3)

    o.assertNoMoreEvents()
    assertThat(source.hasObservers()).isFalse()
    assertThat(lifecycle.hasObservers()).isFalse()
  }
}
