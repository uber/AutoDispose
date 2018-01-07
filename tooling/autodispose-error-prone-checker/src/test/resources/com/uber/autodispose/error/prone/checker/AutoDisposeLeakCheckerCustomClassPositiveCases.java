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

import com.uber.autodispose.ObservableScoper;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public class AutoDisposeLeakCheckerCustomClassPositiveCases extends ComponentWithLifeCycle {
  public void observable_subscribeWithoutAutodispose() {
    // BUG: Diagnostic contains: Always apply an Autodispose scope before subscribing
    Observable.empty().subscribe();
  }

  public void single_subscribeWithoutAutodispose() {
    // BUG: Diagnostic contains: Always apply an Autodispose scope before subscribing
    Single.just(true).subscribe();
  }

  public void completable_subscribeWithoutAutodispose() {
    // BUG: Diagnostic contains: Always apply an Autodispose scope before subscribing
    Completable.complete().subscribe();
  }

  public void maybe_subscribeWithoutAutodispose() {
    // BUG: Diagnostic contains: Always apply an Autodispose scope before subscribing
    Maybe.empty().subscribe();
  }

  public void flowable_subscribeWithoutAutodispose() {
    // BUG: Diagnostic contains: Always apply an Autodispose scope before subscribing
    Flowable.empty().subscribe();
  }
}
