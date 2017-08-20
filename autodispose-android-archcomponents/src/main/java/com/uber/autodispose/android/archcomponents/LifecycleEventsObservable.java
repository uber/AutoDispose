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

package com.uber.autodispose.android.archcomponents;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.RestrictTo;
import com.uber.autodispose.android.internal.AutoDisposeAndroidUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import io.reactivex.subjects.BehaviorSubject;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static com.uber.autodispose.android.internal.AutoDisposeAndroidUtil.isMainThread;

@RestrictTo(LIBRARY)
class LifecycleEventsObservable extends Observable<Event> {

  private final Lifecycle lifecycle;
  private final BehaviorSubject<Event> eventsObservable = BehaviorSubject.create();

  @SuppressWarnings("CheckReturnValue") LifecycleEventsObservable(Lifecycle lifecycle) {
    this.lifecycle = lifecycle;
    // Backfill if already created for boundary checking
    Lifecycle.State currentState = lifecycle.getCurrentState();
    if (currentState.isAtLeast(Lifecycle.State.CREATED)) {
      Event event;
      switch (lifecycle.getCurrentState()) {
        case CREATED:
          event = Event.ON_CREATE;
          break;
        case STARTED:
          event = Event.ON_START;
          break;
        case RESUMED:
          event = Event.ON_RESUME;
          break;
        default:
          event = Event.ON_DESTROY;
          break;
      }
      eventsObservable.onNext(event);
    }
  }

  Event getValue() {
    return eventsObservable.getValue();
  }

  @Override protected void subscribeActual(Observer<? super Event> observer) {
    if (!isMainThread()) {
      observer.onError(
          new IllegalStateException("Lifecycles can only be bound to on the main thread!"));
      return;
    }
    ArchLifecycleObserver archObserver =
        new ArchLifecycleObserver(lifecycle, observer, eventsObservable);
    observer.onSubscribe(archObserver);
    lifecycle.addObserver(archObserver);
  }

  static final class ArchLifecycleObserver extends MainThreadDisposable
      implements LifecycleObserver {
    private final Lifecycle lifecycle;
    private final Observer<? super Event> observer;
    private final BehaviorSubject<Event> eventsObservable;

    ArchLifecycleObserver(Lifecycle lifecycle, Observer<? super Event> observer,
        BehaviorSubject<Event> eventsObservable) {
      this.lifecycle = lifecycle;
      this.observer = observer;
      this.eventsObservable = eventsObservable;
    }

    @Override protected void onDispose() {
      lifecycle.removeObserver(this);
    }

    @OnLifecycleEvent(Event.ON_ANY) void onStateChange(LifecycleOwner owner, Event event) {
      if (!isDisposed()) {
        eventsObservable.onNext(event);
        observer.onNext(event);
      }
    }
  }
}
