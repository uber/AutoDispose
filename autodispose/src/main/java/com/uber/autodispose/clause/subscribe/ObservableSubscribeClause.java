/*
 * Copyright (C) 2017. Uber Technologies
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

package com.uber.autodispose.clause.subscribe;

import com.uber.autodispose.observers.AutoDisposingObserver;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the lambdize steps that match Observable's subscribe overloads.
 */
public interface ObservableSubscribeClause {

  <T> AutoDisposingObserver<T> empty();

  <T> AutoDisposingObserver<T> lambdize(Consumer<? super T> onNext);

  <T> AutoDisposingObserver<T> lambdize(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError);

  <T> AutoDisposingObserver<T> lambdize(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete);

  <T> AutoDisposingObserver<T> lambdize(Observer<T> observer);

  <T> AutoDisposingObserver<T> lambdize(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete,
      Consumer<? super Disposable> onSubscribe);
}
