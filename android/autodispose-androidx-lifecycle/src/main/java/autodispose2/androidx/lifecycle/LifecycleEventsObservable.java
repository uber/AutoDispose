/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.androidx.lifecycle;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.lifecycle.Lifecycle.Event.ON_CREATE;
import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static androidx.lifecycle.Lifecycle.Event.ON_RESUME;
import static androidx.lifecycle.Lifecycle.Event.ON_START;
import static autodispose2.android.internal.AutoDisposeAndroidUtil.isMainThread;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import autodispose2.android.internal.MainThreadDisposable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

@RestrictTo(LIBRARY_GROUP)
class LifecycleEventsObservable extends Observable<Event> {

  private final Lifecycle lifecycle;
  private final BehaviorSubject<Event> eventsObservable = BehaviorSubject.create();

  @SuppressWarnings("CheckReturnValue")
  LifecycleEventsObservable(Lifecycle lifecycle) {
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

  @Override
  protected void subscribeActual(Observer<? super Event> observer) {
    AutoDisposeLifecycleObserver lifecycleObserver =
        new AutoDisposeLifecycleObserver(lifecycle, observer, eventsObservable);
    observer.onSubscribe(lifecycleObserver);
    if (!isMainThread()) {
      observer.onError(
          new IllegalStateException("Lifecycles can only be bound to on the main thread!"));
      return;
    }
    lifecycle.addObserver(lifecycleObserver);
    if (lifecycleObserver.isDisposed()) {
      lifecycle.removeObserver(lifecycleObserver);
    }
  }

  static final class AutoDisposeLifecycleObserver extends MainThreadDisposable
      implements LifecycleObserver {
    private final Lifecycle lifecycle;
    private final Observer<? super Event> observer;
    private final BehaviorSubject<Event> eventsObservable;

    AutoDisposeLifecycleObserver(
        Lifecycle lifecycle,
        Observer<? super Event> observer,
        BehaviorSubject<Event> eventsObservable) {
      this.lifecycle = lifecycle;
      this.observer = observer;
      this.eventsObservable = eventsObservable;
    }

    @Override
    protected void onDispose() {
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
