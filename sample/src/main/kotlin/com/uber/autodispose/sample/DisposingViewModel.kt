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
package com.uber.autodispose.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import com.uber.autodispose.autoDispose
import com.uber.autodispose.recipes.AutoDisposeViewModel
import com.uber.autodispose.sample.repository.NetworkRepository
import com.uber.autodispose.sample.state.DownloadState
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Demo AutoDisposing ViewModel.
 *
 * This ViewModel will subscribe to Rx streams for you and pass along
 * the values through the [viewRelay].
 *
 * Often times, like in the case of network calls, we want our network
 * requests to go on even if there are orientation changes. If you subscribe
 * to your network Rx stream in the view, the request will be cancelled on
 * orientation change (since your streams are disposed) and you will likely
 * have to make a new network call.
 *
 * Since the ViewModel survives configuration changes, it is an ideal place
 * to subscribe to network Rx streams and then pass it along to the UI
 * using a [BehaviorRelay] or LiveData. Since both of them cache the last
 * emitted value, as soon as your Activity/Fragment comes back and resubscribes
 * to the [viewRelay] after orientation change, it will safely get the most
 * updated value.
 *
 * AutoDispose will automatically dispose any pending subscriptions when
 * the [onCleared] method is called since it extends from [AutoDisposeViewModel].
 */
class DisposingViewModel(private val repository: NetworkRepository) : AutoDisposeViewModel() {

  /**
   * The relay to communicate state to the UI.
   *
   * This should be subscribed by the UI to get the latest
   * state updates unaffected by config changes.
   * This could easily be substituted by a LiveData instance
   * since both of them cache the last emitted value.
   */
  private val viewRelay = BehaviorRelay.create<DownloadState>()

  /**
   * Downloads a large image over the network.
   *
   * This could take some time and we wish to show
   * a progress indicator to the user. We setup a
   * [DownloadState] which we will pass to the UI to
   * show our state.
   *
   * We subscribe in ViewModel to survive configuration
   * changes and keep the download request going. As the
   * view will resubscribe to the [viewRelay], it will get
   * the most updated [DownloadState].
   *
   * @see repository
   */
  fun downloadLargeImage() {
    // Notify UI that we're loading network
    viewRelay.accept(DownloadState.Started)
    repository.downloadProgress()
        .subscribeOn(Schedulers.io())
        .doOnDispose { Log.i(TAG, "Disposing subscription from the ViewModel") }
        .autoDispose(this)
        .subscribe({ progress ->
          viewRelay.accept(DownloadState.InProgress(progress))
        }, { error ->
          error.printStackTrace()
        }, {
          viewRelay.accept(DownloadState.Completed)
        })
  }

  /**
   * State representation for our current activity.
   *
   * For the purposes of this demo, we'll use a [String]
   * but you can model your own ViewState with a sealed class
   * and expose that.
   */
  fun downloadState(): Observable<DownloadState> {
    return viewRelay.hide()
  }

  companion object {
    const val TAG = "DisposingViewModel"
  }

  class Factory(private val networkRepository: NetworkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return DisposingViewModel(networkRepository) as T
    }
  }
}
