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

import com.uber.autodispose.observers.AutoDisposingSubscriber;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Subscribe clause for the lambdize steps that match Flowable's subscribe overloads.
 */
public interface FlowableSubscribeClause {

  <T> AutoDisposingSubscriber<T> empty();

  <T> AutoDisposingSubscriber<T> lambdize(Consumer<? super T> onNext);

  <T> AutoDisposingSubscriber<T> lambdize(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError);

  <T> AutoDisposingSubscriber<T> lambdize(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete);

  <T> AutoDisposingSubscriber<T> lambdize(Subscriber<T> subscriber);

  <T> AutoDisposingSubscriber<T> lambdize(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete,
      Consumer<? super Subscription> onSubscribe);
}
