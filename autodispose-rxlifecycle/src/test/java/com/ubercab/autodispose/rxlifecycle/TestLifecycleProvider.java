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

package com.ubercab.autodispose.rxlifecycle;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.trello.rxlifecycle2.RxLifecycle;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

final class TestLifecycleProvider implements LifecycleProvider<TestLifecycleProvider.Event> {

  private static final Function<Event, Event> CORRESPONDING_EVENTS = event -> {
    switch (event) {
      case CREATE:
        return Event.DESTROY;
      default:
        throw new OutsideLifecycleException("Lifecycle ended");
    }
  };

  private final BehaviorSubject<Event> lifecycle = BehaviorSubject.create();

  @Override public Observable<Event> lifecycle() {
    return lifecycle.hide();
  }

  @Override public <T> LifecycleTransformer<T> bindUntilEvent(Event event) {
    return RxLifecycle.bindUntilEvent(lifecycle, event);
  }

  @Override public <T> LifecycleTransformer<T> bindToLifecycle() {
    return RxLifecycle.bind(lifecycle, CORRESPONDING_EVENTS);
  }

  void emitCreate() {
    lifecycle.onNext(Event.CREATE);
  }

  void emitDestroy() {
    lifecycle.onNext(Event.DESTROY);
  }

  enum Event {
    CREATE, DESTROY
  }
}
