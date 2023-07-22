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
package autodispose2.sample

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import autodispose2.ScopeProvider
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import autodispose2.autoDispose
import autodispose2.sample.repository.NetworkRepository
import autodispose2.sample.state.DownloadState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class DisposingViewModelActivity : AppCompatActivity() {

  // Network repository. Can be substituted by DI
  private val networkRepository: NetworkRepository by lazy { NetworkRepository() }
  // The view model factory
  private val viewModelFactory by lazy { DisposingViewModel.Factory(networkRepository) }
  // The ViewModel for this Activity.
  private val viewModel: DisposingViewModel by lazy {
    ViewModelProviders.of(this, viewModelFactory).get(DisposingViewModel::class.java)
  }

  private val scope: ScopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

  private lateinit var textView: TextView
  private lateinit var progressBar: ProgressBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_disposing_viewmodel)

    textView = findViewById(R.id.textView)
    progressBar = findViewById(R.id.downloadProgress)
    progressBar.max = 50

    // If we're coming from a configuration change, no need to
    // start the stream again.
    if (savedInstanceState == null) {
      viewModel.downloadLargeImage()
    }

    // Get latest value from ViewModel unaffected by any config changes.
    viewModel
      .downloadState()
      .observeOn(AndroidSchedulers.mainThread())
      .autoDispose(scope)
      .subscribe({ state -> resolveState(state) }, {})
  }

  /**
   * State resolver for the UI.
   *
   * @param state the download state.
   */
  private fun resolveState(state: DownloadState) {
    when (state) {
      is DownloadState.Started -> {
        textView.setText(R.string.download_started)
      }
      is DownloadState.InProgress -> {
        textView.setText(R.string.download_in_progress)
        progressBar.progress = state.progress
      }
      is DownloadState.Completed -> {
        textView.setText(R.string.download_completed)
      }
    }
  }
}
