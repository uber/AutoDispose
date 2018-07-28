/*
 * Copyright (c) 2018. Uber Technologies
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

package com.uber.autodispose.android.lifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import io.reactivex.subjects.BehaviorSubject;

import static android.arch.lifecycle.Lifecycle.Event.ON_CREATE;
import static android.arch.lifecycle.Lifecycle.Event.ON_DESTROY;
import static android.arch.lifecycle.Lifecycle.Event.ON_RESUME;
import static android.arch.lifecycle.Lifecycle.Event.ON_START;
import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static com.uber.autodispose.android.internal.AutoDisposeAndroidUtil.isMainThread;

@RestrictTo(LIBRARY) class LifecycleEventsObservable extends Observable<Event> {

  private final Lifecycle lifecycle;
  private final BehaviorSubject<Event> eventsObservable = BehaviorSubject.create();

  @SuppressWarnings("CheckReturnValue") LifecycleEventsObservable(Lifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  Event getValue() {
    return eventsObservable.getValue();
  }

  /**
   * Backfill if already created for boundary checking. We do a trick here for corresponding events
   * where we pretend something is created upon initialized state so that it assumes the
   * corresponding event is DESTROY.
   */
  void backfillEvents() {
    @Nullable Lifecycle.Event correspondingEvent;
    switch (lifecycle.getCurrentState()) {
      case INITIALIZED:
        correspondingEvent = ON_CREATE;
        break;
      case CREATED:
        correspondingEvent = ON_START;
        break;
      case STARTED:
      case RESUMED:
        correspondingEvent = ON_RESUME;
        break;
      case DESTROYED:
      default:
        correspondingEvent = ON_DESTROY;
        break;
    }
    eventsObservable.onNext(correspondingEvent);
  }

  @Override protected void subscribeActual(Observer<? super Event> observer) {
    ArchLifecycleObserver archObserver =
        new ArchLifecycleObserver(lifecycle, observer, eventsObservable);
    observer.onSubscribe(archObserver);
    if (!isMainThread()) {
      observer.onError(
          new IllegalStateException("Lifecycles can only be bound to on the main thread!"));
      return;
    }
    lifecycle.addObserver(archObserver);
    if (archObserver.isDisposed()) {
      lifecycle.removeObserver(archObserver);
    }
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

    @OnLifecycleEvent(Event.ON_ANY)
    void onStateChange(@SuppressWarnings("unused") LifecycleOwner owner, Event event) {
      if (!isDisposed()) {
        if (!(event == ON_CREATE && eventsObservable.getValue() == event)) {
          // Due to the INITIALIZED->ON_CREATE mapping trick we do in backfill(),
          // we fire this conditionally to avoid duplicate CREATE events.
          eventsObservable.onNext(event);
        }
        observer.onNext(event);
      }
    }
  }
}
