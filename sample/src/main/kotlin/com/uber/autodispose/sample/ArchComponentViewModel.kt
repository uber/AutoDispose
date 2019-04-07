/*
 * Copyright 2019. Uber Technologies
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
import android.graphics.Bitmap
import com.uber.autodispose.sample.repository.ImageRepository
import io.reactivex.Observable

/**
 * Demo Architecture Component ViewModel. The ViewModel
 * will expose your Rx stream which should be observed
 * by the view.
 */
class ArchComponentViewModel(private val imageRepository: ImageRepository) : ViewModel() {

  /**
   * Calls the repository to get a subscription of the Bitmap.
   * The repository caches the last value of the Observable so that
   * things like orientation changes don't trigger "reloading" of the
   * Bitmap from the raw resource.
   */
  fun image(): Observable<Bitmap> {
    return imageRepository.image()
  }

  /**
   * Load the given [imageId] from resources.
   */
  fun loadBitmap(imageId: Int) {
    imageRepository.loadImage(imageId)
  }

  class Factory(private val imageRepository: ImageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return ArchComponentViewModel(imageRepository) as T
    }
  }
}
