/*
 * Copyright (c) 2016. Uber Technologies
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

import com.uber.autodispose.internal.AutoDisposableHelper;
import com.uber.autodispose.internal.AutoDisposeUtil;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicReference;

public final class AutoDisposingMaybeObserver<T> implements MaybeObserver<T>, Disposable {

  private final AtomicReference<Disposable> mainDisposable = new AtomicReference<>();
  private final AtomicReference<Disposable> lifecycleDisposable = new AtomicReference<>();
  private final Maybe<?> lifecycle;
  private final Consumer<? super T> onSuccess;
  private final Consumer<? super Throwable> onError;
  private final Action onComplete;
  private final Consumer<? super Disposable> onSubscribe;

  AutoDisposingMaybeObserver(Maybe<?> lifecycle, Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError, Action onComplete,
      Consumer<? super Disposable> onSubscribe) {
    this.lifecycle = lifecycle;
    this.onSuccess = AutoDisposeUtil.emptyConsumerIfNull(onSuccess);
    this.onError = AutoDisposeUtil.emptyErrorConsumerIfNull(onError);
    this.onComplete = AutoDisposeUtil.emptyActionIfNull(onComplete);
    this.onSubscribe = AutoDisposeUtil.emptyDisposableIfNull(onSubscribe);
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
          onSubscribe.accept(this);
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

      // If we've never actually called the downstream onSubscribe (i.e. requested immediately in
      // onSubscribe and had a terminal event), we need to still send an empty disposable instance
      // to abide by the Observer contract.
      if (mainDisposable.get() == null) {
        try {
          onSubscribe.accept(Disposables.disposed());
        } catch (Exception e) {
          Exceptions.throwIfFatal(e);
          RxJavaPlugins.onError(e);
        }
      }
      AutoDisposableHelper.dispose(mainDisposable);
    }
  }

  @Override public final void onSuccess(T value) {
    if (!isDisposed()) {
      dispose();
      try {
        onSuccess.accept(value);
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        onError(e);
      }
    }
  }

  @Override public final void onError(Throwable e) {
    if (!isDisposed()) {
      dispose();
      try {
        onError.accept(e);
      } catch (Exception e1) {
        Exceptions.throwIfFatal(e1);
        RxJavaPlugins.onError(new CompositeException(e, e1));
      }
    }
  }

  @Override public final void onComplete() {
    if (!isDisposed()) {
      dispose();
      try {
        onComplete.run();
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        RxJavaPlugins.onError(e);
      }
    }
  }
}
