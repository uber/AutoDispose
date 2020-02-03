/*
 * Copyright (C) 2020. Uber Technologies
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
package autodispose2.androidx.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import autodispose2.ScopeProvider
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

class AndroidViewModelScopeProvider(private val viewModel: ViewModel) : ScopeProvider {

  override fun requestScope(): CompletableSource = Completable.create { emitter ->
    with(viewModel.viewModelScope) {
      if (isActive) {
        coroutineContext[Job]?.invokeOnCompletion {
          emitter.onComplete()
        } ?: emitter.tryOnError(IllegalStateException("coroutineContext doesn't contain Job"))
      } else {
        emitter.onComplete()
      }
    }
  }
}
