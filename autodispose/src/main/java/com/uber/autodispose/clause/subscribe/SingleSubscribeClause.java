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

import com.uber.autodispose.observers.AutoDisposingSingleObserver;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the lambdize steps that match Single's subscribe overloads.
 */
public interface SingleSubscribeClause {

  <T> AutoDisposingSingleObserver<T> empty();

  <T> AutoDisposingSingleObserver<T> lambdize(Consumer<? super T> onSuccess);

  <T> AutoDisposingSingleObserver<T> lambdize(BiConsumer<? super T, ? super Throwable> biConsumer);

  <T> AutoDisposingSingleObserver<T> lambdize(Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError);

  <T> AutoDisposingSingleObserver<T> lambdize(SingleObserver<T> observer);

  <T> AutoDisposingSingleObserver<T> lambdize(Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError,
      Consumer<? super Disposable> onSubscribe);
}
