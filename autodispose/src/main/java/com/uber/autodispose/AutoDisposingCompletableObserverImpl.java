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

package com.uber.autodispose;

import com.uber.autodispose.observers.AutoDisposingCompletableObserver;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicReference;

final class AutoDisposingCompletableObserverImpl implements AutoDisposingCompletableObserver {

  private final AtomicReference<Disposable> mainDisposable = new AtomicReference<>();
  private final AtomicReference<Disposable> lifecycleDisposable = new AtomicReference<>();
  private final Maybe<?> lifecycle;
  private final CompletableObserver delegate;

  AutoDisposingCompletableObserverImpl(Maybe<?> lifecycle, CompletableObserver delegate) {
    this.lifecycle = lifecycle;
    this.delegate = delegate;
  }

  @Override public final void onSubscribe(Disposable d) {
    if (AutoDisposableHelper.setOnce(lifecycleDisposable,
        lifecycle.subscribe(new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            dispose();
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable e1) throws Exception {
            onError(e1);
          }
        }))) {
      if (AutoDisposableHelper.setOnce(mainDisposable, d)) {
        try {
          delegate.onSubscribe(this);
        } catch (Throwable t) {
          Exceptions.throwIfFatal(t);
          d.dispose();
          onError(t);
        }
      }
    }
  }

  @Override public final boolean isDisposed() {
    return mainDisposable.get() == AutoDisposableHelper.DISPOSED;
  }

  @Override public final void dispose() {
    synchronized (this) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      callMainSubscribeIfNecessary();
      AutoDisposableHelper.dispose(mainDisposable);
    }
  }

  private void lazyDispose() {
    synchronized (this) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      callMainSubscribeIfNecessary();
      mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
    }
  }

  private void callMainSubscribeIfNecessary() {
    // If we've never actually called the downstream onSubscribe (i.e. requested immediately in
    // onSubscribe and had a terminal event), we need to still send an empty disposable instance
    // to abide by the Observer contract.
    if (mainDisposable.get() == null) {
      try {
        delegate.onSubscribe(Disposables.disposed());
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        RxJavaPlugins.onError(e);
      }
    }
  }

  @Override public final void onComplete() {
    if (!isDisposed()) {
      lazyDispose();
      try {
        delegate.onComplete();
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        RxJavaPlugins.onError(e);
      }
    }
  }

  @Override public final void onError(Throwable e) {
    if (!isDisposed()) {
      lazyDispose();
      try {
        delegate.onError(e);
      } catch (Exception e1) {
        Exceptions.throwIfFatal(e1);
        RxJavaPlugins.onError(new CompositeException(e, e1));
      }
    }
  }
}
