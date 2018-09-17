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

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers

class DisposingViewModelActivity: AppCompatActivity() {

  private val viewModel: DisposingViewModel by lazy { ViewModelProviders.of(this).get(DisposingViewModel::class.java) }

  private val scope: ScopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

  lateinit var textView: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_disposing_viewmodel)

    textView = findViewById(R.id.textView)

    // If we're coming from a configuration change, no need to
    // start the stream again.
    if (savedInstanceState == null) {
      viewModel.loadNetworkResource()
    }

    // Get latest value from ViewModel unaffected by any config changes.
    viewModel.viewState()
        .observeOn(AndroidSchedulers.mainThread())
        .autoDisposable(scope)
        .subscribe({ value ->
          textView.text = value
        }, {})
  }
}
