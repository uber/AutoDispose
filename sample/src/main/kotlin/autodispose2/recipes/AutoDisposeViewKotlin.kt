/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.recipes

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import autodispose2.android.ViewScopeProvider
import autodispose2.lifecycle.CorrespondingEventsFunction
import autodispose2.lifecycle.LifecycleEndedException
import autodispose2.lifecycle.LifecycleScopeProvider
import autodispose2.recipes.AutoDisposeViewKotlin.ViewEvent
import autodispose2.recipes.AutoDisposeViewKotlin.ViewEvent.ATTACH
import autodispose2.recipes.AutoDisposeViewKotlin.ViewEvent.DETACH
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * An example implementation of an AutoDispose View with lifecycle handling and precondition checks
 * using [LifecycleScopeProvider]. The precondition checks here are only different from what
 * [ViewScopeProvider] provides in that it will check against subscription in the constructor.
 */
abstract class AutoDisposeViewKotlin : View, LifecycleScopeProvider<ViewEvent> {

  enum class ViewEvent {
    ATTACH,
    DETACH
  }

  private val lifecycleEvents by lazy { BehaviorSubject.create<ViewEvent>() }

  @JvmOverloads
  constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = View.NO_ID
  ) : super(context, attrs, defStyleAttr)

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int,
    defStyleRes: Int
  ) : super(context, attrs, defStyleAttr, defStyleRes)

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

  companion object {

    /**
     * This is a function of current event -> target disposal event. That is to say that if event
     * "Attach" returns "Detach", then any stream subscribed to during Attach will autodispose on
     * Detach.
     */
    private val CORRESPONDING_EVENTS =
      CorrespondingEventsFunction<ViewEvent> { viewEvent ->
        when (viewEvent) {
          ATTACH -> DETACH
          else -> throw LifecycleEndedException("Cannot bind to View lifecycle after detach.")
        }
      }
  }
}
