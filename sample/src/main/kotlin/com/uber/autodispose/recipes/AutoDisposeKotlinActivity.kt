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

package com.uber.autodispose.recipes

import android.app.Activity
import android.os.Bundle
import com.uber.autodispose.LifecycleEndedException
import com.uber.autodispose.LifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.subjects.BehaviorSubject

/**
 * An [Activity] example implementation for making one implement [LifecycleScopeProvider].
 */
abstract class AutoDisposeKotlinActivity : Activity(), LifecycleScopeProvider<AutoDisposeKotlinActivity.ActivityEvent> {

  private val lifecycleEvents = BehaviorSubject.create<ActivityEvent>()

  enum class ActivityEvent {
    CREATE, START, RESUME, PAUSE, STOP, DESTROY
  }

  override fun lifecycle(): Observable<ActivityEvent> {
    return lifecycleEvents.hide()
  }

  override fun correspondingEvents(): Function<ActivityEvent, ActivityEvent> {
    return CORRESPONDING_EVENTS
  }

  override fun peekLifecycle(): ActivityEvent? {
    return lifecycleEvents.value
  }

  override fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    lifecycleEvents.onNext(ActivityEvent.CREATE)
  }

  override fun onStart() {
    super.onStart()
    lifecycleEvents.onNext(ActivityEvent.START)
  }

  override fun onResume() {
    super.onResume()
    lifecycleEvents.onNext(ActivityEvent.RESUME)
  }

  override fun onPause() {
    lifecycleEvents.onNext(ActivityEvent.PAUSE)
    super.onPause()
  }

  override fun onStop() {
    lifecycleEvents.onNext(ActivityEvent.STOP)
    super.onStop()
  }

  override fun onDestroy() {
    lifecycleEvents.onNext(ActivityEvent.DESTROY)
    super.onDestroy()
  }

  companion object {

    private val CORRESPONDING_EVENTS = Function<ActivityEvent, ActivityEvent> { activityEvent ->
      when (activityEvent) {
        AutoDisposeKotlinActivity.ActivityEvent.CREATE -> ActivityEvent.DESTROY
        AutoDisposeKotlinActivity.ActivityEvent.START -> ActivityEvent.STOP
        AutoDisposeKotlinActivity.ActivityEvent.RESUME, AutoDisposeKotlinActivity.ActivityEvent.PAUSE, AutoDisposeKotlinActivity.ActivityEvent.STOP -> ActivityEvent.DESTROY
        else -> throw LifecycleEndedException()
      }
    }
  }
}
