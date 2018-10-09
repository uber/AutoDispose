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
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import com.uber.autodispose.android.ViewScopeProvider
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import com.uber.autodispose.lifecycle.LifecycleScopes
import com.uber.autodispose.recipes.AutoDisposeViewKotlin.ViewEvent
import com.uber.autodispose.recipes.AutoDisposeViewKotlin.ViewEvent.ATTACH
import com.uber.autodispose.recipes.AutoDisposeViewKotlin.ViewEvent.DETACH
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * An example implementation of an AutoDispose View with lifecycle handling and precondition checks
 * using [LifecycleScopeProvider]. The precondition checks here are only different from what
 * [ViewScopeProvider] provides in that it will check against subscription in the constructor.
 */
abstract class AutoDisposeViewKotlin : View, LifecycleScopeProvider<ViewEvent> {

  enum class ViewEvent {
    ATTACH, DETACH
  }

  private val lifecycleEvents by lazy { BehaviorSubject.create<ViewEvent>() }

  @JvmOverloads constructor(
      context: Context,
      attrs: AttributeSet? = null,
      defStyleAttr: Int = View.NO_ID)
      : super(context, attrs, defStyleAttr)

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
      : super(context, attrs, defStyleAttr, defStyleRes)

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    lifecycleEvents.onNext(ViewEvent.ATTACH)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    lifecycleEvents.onNext(ViewEvent.DETACH)
  }

  override fun lifecycle(): Observable<ViewEvent> = lifecycleEvents.hide()

  override fun correspondingEvents(): CorrespondingEventsFunction<ViewEvent> {
    return CORRESPONDING_EVENTS
  }

  override fun peekLifecycle(): ViewEvent? {
    return lifecycleEvents.value
  }

  override fun requestScope(): CompletableSource {
    return LifecycleScopes.resolveScopeFromLifecycle(this)
  }

  companion object {

    /**
     * This is a function of current event -> target disposal event. That is to say that if event
     * "Attach" returns "Detach", then any stream subscribed to during Attach will autodispose on
     * Detach.
     */
    private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<ViewEvent> { viewEvent ->
      when (viewEvent) {
        ATTACH -> DETACH
        else -> throw LifecycleEndedException(
            "Cannot bind to View lifecycle after detach.")
      }
    }
  }
}
