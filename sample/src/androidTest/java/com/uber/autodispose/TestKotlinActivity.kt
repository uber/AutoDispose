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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.uber.autodispose.android.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.android.lifecycle.autoDisposable
import com.uber.autodispose.android.lifecycle.scope
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.Flowable
import org.junit.Ignore
import java.util.concurrent.TimeUnit

/**
 * Test Activity class to verify compilation of various extension functions.
 */
@Ignore("Since it's only used to verify compilation of the extension functions")
class TestKotlinActivity : AppCompatActivity(), ScopeProvider {

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

    Maybe.just(1)
        .autoDisposable(this)
        .subscribe()

    Maybe.just(1)
        .autoDisposable(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    Flowable.just(1)
        .autoDisposable(this)
        .subscribe()

    Flowable.just(1)
        .autoDisposable(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    Single.just(1)
        .autoDisposable(this)
        .subscribe()

    Single.just(1)
        .autoDisposable(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    Completable.never()
        .autoDisposable(this)
        .subscribe()

    Completable.never()
        .autoDisposable(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    val rootView = findViewById<View>(android.R.id.content)

    // Taking scope of a View
    Observable.interval(1, TimeUnit.DAYS)
        .autoDisposable(rootView)
        .subscribe()
  }

  override fun requestScope(): CompletableSource {
    return Completable.complete()
  }
}
