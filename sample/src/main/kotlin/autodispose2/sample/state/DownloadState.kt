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
package autodispose2.sample.state

/**
 * State representation for when we're downloading a file.
 *
 * This is used in your UI for proper state management.
 */
sealed class DownloadState {

  /** Download started. */
  object Started : DownloadState()

  /**
   * Download is in progress.
   *
   * @param progress the progress so far.
   */
  class InProgress(val progress: Int) : DownloadState()

  /** Download has completed. */
  object Completed : DownloadState()
}
