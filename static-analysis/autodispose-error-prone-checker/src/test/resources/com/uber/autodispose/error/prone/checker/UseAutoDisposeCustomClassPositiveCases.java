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

package com.uber.autodispose.error.prone.checker;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.ObservableScoper;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.reactivestreams.Subscriber;

public class UseAutoDisposeCustomClassPositiveCases extends ComponentWithLifeCycle {
  public void observable_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Observable.empty().subscribe();
  }

  public void single_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Single.just(true).subscribe();
  }

  public void completable_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Completable.complete().subscribe();
  }

  public void maybe_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Maybe.empty().subscribe();
  }

  public void flowable_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Flowable.empty().subscribe();
  }

  public void parallelFlowable_subscribeWithoutAutoDispose() {
    Subscriber<Integer>[] subscribers = new Subscriber[] {};
    Flowable.just(1, 2)
        .parallel(2)
        // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
        .subscribe(subscribers);
  }
}
