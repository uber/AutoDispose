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
import com.uber.autodispose.ScopeProvider
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject

private object NOTIFICATION

/**
 * Example implementation of a [ViewHolder] implementation that implements
 * [ScopeProvider]. This could be useful for cases where you have subscriptions that should be
 * disposed upon unbinding or otherwise aren't overwritten in future binds.
 */
abstract class AutoDisposeViewHolderKotlin(itemView: View)
  : BindAwareViewHolder(itemView), ScopeProvider {

  private var unbindNotifier: MaybeSubject<Any>? = null

  private val notifier: MaybeSubject<Any>
    get() {
      synchronized(this) {
        var n = unbindNotifier
        return if (n == null) {
          n = MaybeSubject.create<Any>()
          unbindNotifier = n
          n
        } else {
          n
        }
      }
    }

  override fun onUnbind() {
    emitUnBindIfPresent()
    unbindNotifier = null
  }

  private fun emitUnBindIfPresent() {
    unbindNotifier?.let {
      if (!it.hasComplete()) {
        it.onSuccess(NOTIFICATION)
      }
    }
  }

  override fun requestScope(): Maybe<*> {
    return notifier
  }

}
