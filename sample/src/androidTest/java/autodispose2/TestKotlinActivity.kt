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
package autodispose2

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import autodispose2.android.autoDispose
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import autodispose2.androidx.lifecycle.autoDispose
import autodispose2.androidx.lifecycle.scope
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableSource
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
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
        .autoDispose(this)
        .subscribe()

    // With extension function that overloads LifecycleOwner and until Event
    Observable.interval(1, TimeUnit.SECONDS)
        .autoDispose(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    // With extension function that overloads ScopeProvider
    Observable.interval(1, TimeUnit.SECONDS)
        .autoDispose(scope(Lifecycle.Event.ON_DESTROY))
        .subscribe()

    // With no extension function
    Observable.interval(1, TimeUnit.SECONDS)
        .autoDispose(AndroidLifecycleScopeProvider.from(this))
        .subscribe()

    Maybe.just(1)
        .autoDispose(this)
        .subscribe()

    Maybe.just(1)
        .autoDispose(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    Flowable.just(1)
        .autoDispose(this)
        .subscribe()

    Flowable.just(1)
        .autoDispose(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    Single.just(1)
        .autoDispose(this)
        .subscribe()

    Single.just(1)
        .autoDispose(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    Completable.never()
        .autoDispose(this)
        .subscribe()

    Completable.never()
        .autoDispose(this, Lifecycle.Event.ON_DESTROY)
        .subscribe()

    val rootView = findViewById<View>(android.R.id.content)

    // Taking scope of a View
    Observable.interval(1, TimeUnit.DAYS)
        .autoDispose(rootView)
        .subscribe()

    // TODO re-enable with rxlifecycle interop
    // RxLifecycle
//    val lifecycleProvider = TestLifecycleProvider()
//    Observable.interval(1, TimeUnit.SECONDS)
//        .autoDispose(lifecycleProvider)
//        .subscribe()
//
//    Observable.interval(1, TimeUnit.SECONDS)
//        .autoDispose(lifecycleProvider, TestLifecycleProvider.Event.CREATE)
//        .subscribe()
  }

  override fun requestScope(): CompletableSource {
    return Completable.complete()
  }

//  /** Stub implementation for [LifecycleProvider] for compilation testing */
//  class TestLifecycleProvider : LifecycleProvider<TestLifecycleProvider.Event> {
//    override fun lifecycle(): Observable<Event> {
//      return Observable.empty<Event>()
//    }
//
//    override fun <T : Any?> bindUntilEvent(event: Event): LifecycleTransformer<T> {
//      TODO("Stub to test compilation of extensions that use LifecycleProvider")
//    }
//
//    override fun <T : Any?> bindToLifecycle(): LifecycleTransformer<T> {
//      TODO("Stub to test compilation of extensions that use LifecycleProvider")
//    }
//
//    enum class Event {
//      CREATE,
//      DESTROY
//    }
//  }
}
