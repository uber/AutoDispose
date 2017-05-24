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
import android.arch.lifecycle.Lifecycle.State;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.RestrictTo;
import com.uber.autodispose.android.internal.AutoDisposeAndroidUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
class LifecycleStatesObservable extends Observable<State> {

  private final Lifecycle lifecycle;

  LifecycleStatesObservable(Lifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  @Override protected void subscribeActual(Observer<? super State> observer) {
    if (!AutoDisposeAndroidUtil.isMainThread()) {
      observer.onError(
          new IllegalStateException("Lifecycles can only be bound to on the main thread!"));
      return;
    }

    ArchLifecycleObserver archObserver = new ArchLifecycleObserver(lifecycle, observer);
    observer.onSubscribe(archObserver);
    lifecycle.addObserver(archObserver);
  }

  static final class ArchLifecycleObserver extends MainThreadDisposable
      implements LifecycleObserver {
    private final Lifecycle lifecycle;
    private final Observer<? super State> observer;

    ArchLifecycleObserver(Lifecycle lifecycle, Observer<? super State> observer) {
      this.lifecycle = lifecycle;
      this.observer = observer;
    }

    @Override protected void onDispose() {
      lifecycle.removeObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY) void onStateChange() {
      if (!isDisposed()) {
        observer.onNext(lifecycle.getCurrentState());
      }
    }
  }
}
