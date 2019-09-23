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

import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

/**
 * Repository layer for your network requests.
 *
 * Simulates the download request and sends the
 * progress so far.
 */
class NetworkRepository {

  /**
   * Emit the progress of the requested download.
   *
   * We simulate the download progress by emitting
   * an int periodically and call onComplete after
   * it's done.
   *
   * @return [Observable] emitting download progress.
   */
  fun downloadProgress(): Observable<Int> {
    return Observable.interval(500, TimeUnit.MILLISECONDS)
        .map { it.toInt() }
        .take(51)
  }
}
