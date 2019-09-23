/*
 * Copyright (C) 2019. Uber Technologies
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
package com.uber.autodispose.android;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static com.uber.autodispose.android.internal.AutoDisposeAndroidUtil.isMainThread;

import android.os.Build;
import android.view.View;
import androidx.annotation.RestrictTo;
import autodispose2.OutsideScopeException;
import io.reactivex.rxjava3.android.MainThreadDisposable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.CompletableSource;

@RestrictTo(LIBRARY)
final class DetachEventCompletable implements CompletableSource {
  private final View view;

  DetachEventCompletable(View view) {
    this.view = view;
  }

  @Override
  public void subscribe(CompletableObserver observer) {
    Listener listener = new Listener(view, observer);
    observer.onSubscribe(listener);

    // Check we're on the main thread.
    if (!isMainThread()) {
      observer.onError(new IllegalStateException("Views can only be bound to on the main thread!"));
      return;
    }

    // Check that it's attached.
    boolean isAttached =
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && view.isAttachedToWindow())
            || view.getWindowToken() != null;
    if (!isAttached) {
      observer.onError(new OutsideScopeException("View is not attached!"));
      return;
    }

    view.addOnAttachStateChangeListener(listener);
    if (listener.isDisposed()) {
      view.removeOnAttachStateChangeListener(listener);
    }
  }

  static final class Listener extends MainThreadDisposable
      implements View.OnAttachStateChangeListener {
    private final View view;
    private final CompletableObserver observer;

    Listener(View view, CompletableObserver observer) {
      this.view = view;
      this.observer = observer;
    }

    @Override
    public void onViewAttachedToWindow(View v) {}

    @Override
    public void onViewDetachedFromWindow(View v) {
      if (!isDisposed()) {
        observer.onComplete();
      }
    }

    @Override
    protected void onDispose() {
      view.removeOnAttachStateChangeListener(this);
    }
  }
}
