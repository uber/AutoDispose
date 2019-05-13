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
package com.uber.autodispose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.android.lifecycle.autoDisposable
import com.uber.autodispose.android.lifecycle.scope
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Test Activity class to verify compilation of various extension functions.
 */
class TestKotlinActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // With extension function that overloads LifecycleOwner
    Observable.interval(1, TimeUnit.SECONDS)
        .autoDisposable(this)
        .subscribe()

    // With extension function that overloads LifecycleOwner and until Event
    Observable.interval(1, TimeUnit.SECONDS)
        .autoDisposable(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    // With extension function that overloads ScopeProvider
    Observable.interval(1, TimeUnit.SECONDS)
        .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
        .subscribe()

    // With no extension function
    Observable.interval(1, TimeUnit.SECONDS)
        .autoDisposable(AndroidLifecycleScopeProvider.from(this))
        .subscribe()
  }
}
