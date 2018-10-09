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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import com.uber.autodispose.lifecycle.LifecycleScopes
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.ATTACH
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.CREATE
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.CREATE_VIEW
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.DESTROY
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.DESTROY_VIEW
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.DETACH
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.PAUSE
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.RESUME
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.START
import com.uber.autodispose.recipes.AutoDisposeFragmentKotlin.FragmentEvent.STOP
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * A [Fragment] example implementation for making one implement [LifecycleScopeProvider]. One would
 * normally use this as a base fragment class to extend others from.
 */
abstract class AutoDisposeFragmentKotlin : Fragment(), LifecycleScopeProvider<FragmentEvent> {

  private val lifecycleEvents = BehaviorSubject.create<FragmentEvent>()

  enum class FragmentEvent {
    ATTACH, CREATE, CREATE_VIEW, START, RESUME, PAUSE, STOP, DESTROY_VIEW, DESTROY, DETACH
  }

  override fun lifecycle(): Observable<FragmentEvent> {
    return lifecycleEvents.hide()
  }

  override fun correspondingEvents(): CorrespondingEventsFunction<FragmentEvent> {
    return CORRESPONDING_EVENTS
  }

  override fun peekLifecycle(): FragmentEvent? {
    return lifecycleEvents.value
  }

  override fun requestScope(): CompletableSource {
    return LifecycleScopes.resolveScopeFromLifecycle(this)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    lifecycleEvents.onNext(FragmentEvent.ATTACH)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycleEvents.onNext(FragmentEvent.CREATE)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycleEvents.onNext(FragmentEvent.CREATE_VIEW)
  }

  override fun onStart() {
    super.onStart()
    lifecycleEvents.onNext(FragmentEvent.START)
  }

  override fun onResume() {
    super.onResume()
    lifecycleEvents.onNext(FragmentEvent.RESUME)
  }

  override fun onPause() {
    lifecycleEvents.onNext(FragmentEvent.PAUSE)
    super.onPause()
  }

  override fun onStop() {
    lifecycleEvents.onNext(FragmentEvent.STOP)
    super.onStop()
  }

  override fun onDestroyView() {
    lifecycleEvents.onNext(FragmentEvent.DESTROY_VIEW)
    super.onDestroyView()
  }

  override fun onDestroy() {
    lifecycleEvents.onNext(FragmentEvent.DESTROY)
    super.onDestroy()
  }

  override fun onDetach() {
    lifecycleEvents.onNext(FragmentEvent.DETACH)
    super.onDetach()
  }

  companion object {

    /**
     * This is a function of current event -> target disposal event. That is to say that if event A
     * returns B, then any stream subscribed to during A will autodispose on B. In Android, we make
     * symmetric boundary conditions. Create -> Destroy, Start -> Stop, etc. For anything after
     * Resume we dispose on the next immediate destruction event. Subscribing after Detach is an
     * error.
     */
    private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<FragmentEvent> { event ->
      when (event) {
        ATTACH -> DETACH
        CREATE -> DESTROY
        CREATE_VIEW -> DESTROY_VIEW
        START -> STOP
        RESUME -> PAUSE
        PAUSE -> STOP
        STOP -> DESTROY_VIEW
        DESTROY_VIEW -> DESTROY
        DESTROY -> DETACH
        else -> throw LifecycleEndedException(
            "Cannot bind to Fragment lifecycle after detach.")
      }
    }
  }
}
