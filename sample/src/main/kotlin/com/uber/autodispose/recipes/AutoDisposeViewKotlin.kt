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
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import com.uber.autodispose.LifecycleEndedException
import com.uber.autodispose.LifecycleScopeProvider
import com.uber.autodispose.android.ViewScopeProvider
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.subjects.BehaviorSubject

/**
 * An example implementation of an AutoDispose View with lifecycle handling and precondition checks
 * using [LifecycleScopeProvider]. The precondition checks here are only different from what
 * [ViewScopeProvider] provides in that it will check against subscription in the constructor.
 */
abstract class AutoDisposeViewKotlin : View, LifecycleScopeProvider<AutoDisposeViewKotlin.ViewEvent> {

  enum class ViewEvent {
    ATTACH, DETACH
  }

  private var lifecycleEvents: BehaviorSubject<ViewEvent>? = null

  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
      defStyleAttr: Int = View.NO_ID) : super(context, attrs, defStyleAttr)

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
      context, attrs, defStyleAttr, defStyleRes)

  init {
    if (!isInEditMode) {
      // This is important to gate so you don't break the IDE preview!
      lifecycleEvents = BehaviorSubject.create()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (lifecycleEvents != null) {
      lifecycleEvents!!.onNext(ViewEvent.ATTACH)
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    if (lifecycleEvents != null) {
      lifecycleEvents!!.onNext(ViewEvent.DETACH)
    }
  }

  override fun lifecycle(): Observable<ViewEvent> {
    return lifecycleEvents!!.hide()
  }

  override fun correspondingEvents(): Function<ViewEvent, ViewEvent> {
    return CORRESPONDING_EVENTS
  }

  override fun peekLifecycle(): ViewEvent? {
    return lifecycleEvents!!.value
  }

  companion object {

    private val CORRESPONDING_EVENTS = Function<ViewEvent, ViewEvent> { viewEvent ->
      when (viewEvent) {
        AutoDisposeViewKotlin.ViewEvent.ATTACH -> ViewEvent.DETACH
        else -> throw LifecycleEndedException("Cannot bind to View lifecycle after detach.")
      }
    }
  }
}
