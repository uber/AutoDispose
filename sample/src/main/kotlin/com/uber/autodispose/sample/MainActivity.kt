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

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.uber.autodispose.android.lifecycle.AndroidLifecycle
import com.uber.autodispose.kotlin.autoDisposeWith
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

  companion object {
    val LOG_TAG = "AD_LIFECYCLE_LOGGING"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(LOG_TAG, "onCreate")
    setContentView(R.layout.activity_main)
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)

    val fab = findViewById<View>(R.id.fab) as FloatingActionButton
    fab.setOnClickListener { view ->
      Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
          .setAction("Action", null)
          .show()
    }
  }

  override fun onStart() {
    super.onStart()
    Log.d(LOG_TAG, "onStart")
    Observable.interval(0, 1, TimeUnit.SECONDS)
        .doOnDispose { println("dispose") }
        .autoDisposeWith(AndroidLifecycle.from(this))
        .subscribe {
          Log.d(LOG_TAG, "emit:" + it)
        }
  }

  override fun onResume() {
    super.onResume()
    Log.d(LOG_TAG, "onResume")
  }

  override fun onPause() {
    Log.d(LOG_TAG, "onPause")
    super.onPause()
  }

  override fun onStop() {
    Log.d(LOG_TAG, "onStop")
    super.onStop()
  }

  override fun onDestroy() {
    Log.d(LOG_TAG, "onDestroy")
    super.onDestroy()
  }
}
