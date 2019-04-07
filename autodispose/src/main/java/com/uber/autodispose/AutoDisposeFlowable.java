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
package com.uber.autodispose;

import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

final class AutoDisposeFlowable<T> extends Flowable<T> {
  private final Publisher<T> source;
  private final CompletableSource scope;

  AutoDisposeFlowable(Publisher<T> source, CompletableSource scope) {
    this.source = source;
    this.scope = scope;
  }

  @Override
  protected void subscribeActual(Subscriber<? super T> observer) {
    source.subscribe(new AutoDisposingSubscriberImpl<>(scope, observer));
  }
}
