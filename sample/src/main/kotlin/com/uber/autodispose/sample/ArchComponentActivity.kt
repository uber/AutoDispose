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
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ArchComponentActivity: AppCompatActivity() {

  companion object {
    private val TAG = "AutoDispose-Kotlin"
  }

  private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

  // Custom view model factory
  private val viewModelFactory by lazy { ArchComponentViewModel.Factory(ImageRepository(resources)) }

  // ViewModel for given Activity
  private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory).get(ArchComponentViewModel::class.java) }

  lateinit var imageView: ImageView
  lateinit var button: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(TAG, "onCreate()")
    setContentView(R.layout.activity_arch_component)

    imageView = findViewById(R.id.imageView)
    button = findViewById(R.id.button)

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onDestroy (the opposite of onCreate).
    viewModel.image()
        .doOnDispose { Log.i(TAG, "Disposing ViewModel observer from onCreate()") }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .autoDisposable(scopeProvider)
        .subscribe { bitmap ->
          imageView.setImageBitmap(bitmap)
        }

    // Set listener to load the image.
    button.setOnClickListener {
      viewModel.loadBitmap(R.raw.sunset)
    }
  }

  override fun onStart() {
    Log.d(TAG, "onStart()")
    super.onStart()
  }

  override fun onStop() {
    Log.d(TAG, "onStop()")
    super.onStop()
  }

  override fun onDestroy() {
    Log.d(TAG, "onDestroy()")
    super.onDestroy()
  }

}