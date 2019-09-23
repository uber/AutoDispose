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
package autodispose2.sample.repository

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.RawRes
import com.jakewharton.rx.replayingShare
import com.jakewharton.rxrelay2.PublishRelay
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Observable

/**
 * Repository layer for loading a Bitmap from res/raw.
 * If you're using DI, this should be a singleton class,
 * so that [imageObservable] can be reused across
 * activities/fragments.
 *
 * @param resources
 */
class ImageRepository(private val resources: Resources) {

  // Relay to accept the raw image ids that are to be loaded.
  private val relay = PublishRelay.create<Int>()

  /**
   * The Observable returned to the UI. This will map each given
   * resourceId to a Bitmap and cache it using [replayingShare].
   *
   * The caching is necessary since this is an expensive operation
   * and should not be triggered every time someone subscribes to it.
   * [replayingShare] helps with that.
   */
  private val imageObservable = RxJavaBridge.toV3Observable(
      relay
          .map {
            return@map BitmapFactory.decodeStream(resources.openRawResource(it))
          }
          .replayingShare()
  )

  /**
   * Returns an Observable<Bitmap> that will be consumed in the
   * UI to subscribe to a stream of Bitmaps requested by the user.
   *
   * @return stream of Bitmaps
   */
  fun image(): Observable<Bitmap> {
    return imageObservable
  }

  /**
   * Fires off an event on the relay that will trigger the loading
   * of the Bitmap from given resource [id].
   *
   * @param id the id of the raw resource
   */
  fun loadImage(@RawRes id: Int) {
    relay.accept(id)
  }
}
