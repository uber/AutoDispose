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

package com.uber.autodispose.android;

import android.support.annotation.RestrictTo;
import android.view.View;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static com.uber.autodispose.android.AutoDisposeAndroidUtil.isMainThread;
import static com.uber.autodispose.android.ViewLifecycleEvent.ATTACH;
import static com.uber.autodispose.android.ViewLifecycleEvent.DETACH;

@RestrictTo(LIBRARY)
final class ViewAttachEventsObservable extends Observable<ViewLifecycleEvent> {
  private final View view;

  ViewAttachEventsObservable(View view) {
    this.view = view;
  }

  @Override protected void subscribeActual(Observer<? super ViewLifecycleEvent> observer) {
    if (!isMainThread()) {
      observer.onError(new IllegalStateException("Views can only be bound to on the main thread!"));
      return;
    }

    if (AutoDisposeAndroidUtil.isAttached(view)) {
      // Emit the last event, like a behavior subject
      observer.onNext(ViewLifecycleEvent.ATTACH);
    }
    Listener listener = new Listener(view, observer);
    observer.onSubscribe(listener);
    view.addOnAttachStateChangeListener(listener);
  }

  static final class Listener extends MainThreadDisposable
      implements View.OnAttachStateChangeListener {
    private final View view;
    private final Observer<? super ViewLifecycleEvent> observer;

    Listener(View view, Observer<? super ViewLifecycleEvent> observer) {
      this.view = view;
      this.observer = observer;
    }

    @Override public void onViewAttachedToWindow(View v) {
      if (!isDisposed()) {
        observer.onNext(ATTACH);
      }
    }

    @Override public void onViewDetachedFromWindow(View v) {
      if (!isDisposed()) {
        observer.onNext(DETACH);
      }
    }

    @Override protected void onDispose() {
      view.removeOnAttachStateChangeListener(this);
    }
  }
}
