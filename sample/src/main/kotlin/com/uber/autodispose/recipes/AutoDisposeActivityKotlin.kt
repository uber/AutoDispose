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
package com.uber.autodispose.recipes

import android.app.Activity
import android.os.Bundle
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import com.uber.autodispose.recipes.AutoDisposeActivityKotlin.ActivityEvent
import com.uber.autodispose.recipes.AutoDisposeActivityKotlin.ActivityEvent.CREATE
import com.uber.autodispose.recipes.AutoDisposeActivityKotlin.ActivityEvent.DESTROY
import com.uber.autodispose.recipes.AutoDisposeActivityKotlin.ActivityEvent.PAUSE
import com.uber.autodispose.recipes.AutoDisposeActivityKotlin.ActivityEvent.RESUME
import com.uber.autodispose.recipes.AutoDisposeActivityKotlin.ActivityEvent.START
import com.uber.autodispose.recipes.AutoDisposeActivityKotlin.ActivityEvent.STOP
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * An [Activity] example implementation for making one implement [LifecycleScopeProvider]. One
 * would normally use this as a base activity class to extend others from.
 */
abstract class AutoDisposeActivityKotlin : Activity(), LifecycleScopeProvider<ActivityEvent> {

  private val lifecycleEvents = BehaviorSubject.create<ActivityEvent>()

  enum class ActivityEvent {
    CREATE, START, RESUME, PAUSE, STOP, DESTROY
  }

  override fun lifecycle(): Observable<ActivityEvent> {
    return lifecycleEvents.hide()
  }

  override fun correspondingEvents(): CorrespondingEventsFunction<ActivityEvent> {
    return CORRESPONDING_EVENTS
  }

  override fun peekLifecycle(): ActivityEvent? {
    return lifecycleEvents.value
  }

  override fun onCreate(savedInstanceState: Bundle?) {
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

    /**
     * This is a function of current event -> target disposal event. That is to say that if event A
     * returns B, then any stream subscribed to during A will autodispose on B. In Android, we make
     * symmetric boundary conditions. Create -> Destroy, Start -> Stop, etc. For anything after
     * Resume we dispose on the next immediate destruction event. Subscribing after Destroy is an
     * error.
     */
    private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<ActivityEvent> { activityEvent ->
      when (activityEvent) {
        CREATE -> DESTROY
        START -> STOP
        RESUME -> PAUSE
        PAUSE -> STOP
        STOP -> DESTROY
        else -> throw LifecycleEndedException(
            "Cannot bind to Activity lifecycle after destroy.")
      }
    }
  }
}
