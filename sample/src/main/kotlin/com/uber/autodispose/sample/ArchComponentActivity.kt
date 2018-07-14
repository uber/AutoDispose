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

package com.uber.autodispose.sample

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import com.uber.autodispose.recipes.subscribeBy
import io.reactivex.android.schedulers.AndroidSchedulers

class ArchComponentActivity: AppCompatActivity() {

  // Can be reused
  private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }
  // ViewModel for given Activity
  private val viewModel by lazy { ViewModelProviders.of(this).get(ArchComponentViewModel::class.java) }

  lateinit var textview: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(TAG, "onCreate()")
    setContentView(R.layout.activity_arch_component)

    textview = findViewById(R.id.textview)

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onDestroy (the opposite of onCreate).
    viewModel.observable()
        .doOnDispose { Log.i(TAG, "Disposing ViewModel observer from onCreate()") }
        .observeOn(AndroidSchedulers.mainThread())
        .autoDisposable(scopeProvider)
        .subscribeBy { num ->
          textview.text = "$num emissions"
          Log.i(TAG, "Started in onCreate(), running until onDestroy(): $num")
        }

  }

  override fun onStart() {
    super.onStart()
    Log.d(TAG, "onStart()")
  }

  override fun onStop() {
    super.onStop()
    Log.d(TAG, "onStop()")
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d(TAG, "onDestroy()")
  }

  companion object {
    private val TAG = "AutoDispose-Kotlin"
  }

}