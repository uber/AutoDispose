/*
 * Copyright (c) 2018. Uber Technologies
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

package com.uber.autodispose.sample

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.recipes.AutoDisposeViewModel
import com.uber.autodispose.recipes.subscribeBy
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class DisposingViewModel: AutoDisposeViewModel() {

  /**
   * The relay to communicate state to the UI.
   *
   * This should be subscribed by the UI to get the latest
   * state updates unaffected by config changes.
   * This could easily be substituted by a LiveData instance.
   */
  private val viewRelay = BehaviorRelay.create<String>()

  /**
   * Meant to model a long standing operation.
   *
   * We first tell the UI that we're loading from network.
   * We introduce a artificial delay of 10s to load the actual
   * network. If the config changes, the network will still continue
   * to fetch the resource, unaffected by disposal of the [viewRelay].
   * Whenever it's ready, it will pass it along to the [viewRelay].
   */
  fun loadNetworkResource() {
    // Notify UI that we're loading network
    viewRelay.accept("Loading from network")
    Observable.just("Network loaded")
        .delay(10, TimeUnit.SECONDS)
        .doOnDispose { Log.i(TAG, "Disposing subscription from the ViewModel") }
        .autoDisposable(this)
        .subscribe({
          // Loading done. Pass along to the UI
          viewRelay.accept(it)
        }, { _ ->})
  }

  /**
   * State representation for our current activity.
   *
   * For the purposes of this demo, we'll use a [String]
   * but you can model your own ViewState with a sealed class
   * and expose that.
   */
  fun viewState(): Observable<String> {
    return viewRelay.hide()
  }

  companion object {
    const val TAG = "DisposingViewModel"
  }
}
