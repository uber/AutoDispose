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

import android.support.v7.widget.BindAwareViewHolder
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import com.uber.autodispose.LifecycleEndedException
import com.uber.autodispose.LifecycleScopeProvider
import com.uber.autodispose.recipes.AutoDisposeViewHolderKotlin.ViewHolderEvent.BIND
import com.uber.autodispose.recipes.AutoDisposeViewHolderKotlin.ViewHolderEvent.UNBIND
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.subjects.BehaviorSubject

private object NOTIFICATION

/**
 * Example implementation of a [ViewHolder] implementation that implements
 * [LifecycleScopeProvider]. This could be useful for cases where you have subscriptions that should be
 * disposed upon unbinding or otherwise aren't overwritten in future binds.
 */
abstract class AutoDisposeViewHolderKotlin(itemView: View)
  : BindAwareViewHolder(itemView), LifecycleScopeProvider<AutoDisposeViewHolderKotlin.ViewHolderEvent> {

  private val lifecycleEvents = BehaviorSubject.create<ViewHolderEvent>()

  enum class ViewHolderEvent {
    BIND, UNBIND
  }

  override fun onBind() {
    lifecycleEvents.onNext(BIND)
  }

  override fun onUnbind() {
    lifecycleEvents.onNext(UNBIND)
  }

  override fun lifecycle(): Observable<ViewHolderEvent> {
    return lifecycleEvents.hide()
  }

  override fun correspondingEvents(): Function<ViewHolderEvent, ViewHolderEvent> {
    return CORRESPONDING_EVENTS
  }

  override fun peekLifecycle(): ViewHolderEvent? {
    return lifecycleEvents.value
  }

  companion object {

    private val CORRESPONDING_EVENTS = Function<ViewHolderEvent, ViewHolderEvent> { viewHolderEvent ->
      when (viewHolderEvent) {
        BIND -> UNBIND
        else -> throw LifecycleEndedException("Cannot use view holder lifecycle after unbind.")
      }
    }
  }
}
