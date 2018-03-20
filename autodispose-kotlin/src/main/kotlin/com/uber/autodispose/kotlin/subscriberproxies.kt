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
package com.uber.autodispose.kotlin

import com.uber.autodispose.*
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.plugins.RxJavaPlugins

/*
 * Extension functions on the objects returned by `AutoDisposeConverter`.
 *
 * AutoDispose returns proxy objects that don't extend Observable or the other reactive classes. This means
 * that extensions like RxKotlin's `Observable.subscribeBy` can't be used.
 *
 * These extension functions can be called in the following manner:
 *
 * ```
 * Observable.just(1)
 *   .autoDisposable(this)
 *   .subscribeBy(onError = { Log.e(it) })
 * ```
 */

private val onNextStub: (Any) -> Unit = {}
private val onErrorStub: (Throwable) -> Unit = { RxJavaPlugins.onError(OnErrorNotImplementedException(it)) }
private val onCompleteStub: () -> Unit = {}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun <T : Any> ObservableSubscribeProxy<T>.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub,
  onNext: (T) -> Unit = onNextStub
): Disposable {
  return if (onError === onErrorStub && onComplete === onCompleteStub) {
    subscribe(onNext)
  } else {
    subscribe(onNext, onError, onComplete)
  }
}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun <T : Any> FlowableSubscribeProxy<T>.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub,
  onNext: (T) -> Unit = onNextStub
): Disposable {
  return if (onError === onErrorStub && onComplete === onCompleteStub) {
    subscribe(onNext)
  } else {
    subscribe(onNext, onError, onComplete)
  }
}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun <T : Any> SingleSubscribeProxy<T>.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onSuccess: (T) -> Unit = onNextStub
): Disposable {
  return if (onError === onErrorStub) {
    subscribe(onSuccess)
  } else {
    subscribe(onSuccess, onError)
  }
}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun <T : Any> MaybeSubscribeProxy<T>.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub,
  onSuccess: (T) -> Unit = onNextStub
): Disposable {
  return if (onError === onErrorStub && onComplete === onCompleteStub) {
    subscribe(onSuccess)
  } else {
    subscribe(onSuccess, onError, onComplete)
  }
}


/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun CompletableSubscribeProxy.subscribeBy(
  onError: (Throwable) -> Unit = onErrorStub,
  onComplete: () -> Unit = onCompleteStub
): Disposable {
  return if (onError === onErrorStub) {
    subscribe(onComplete)
  } else {
    subscribe(onComplete, onError)
  }
}
