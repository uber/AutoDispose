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
import io.reactivex.observers.DisposableMaybeObserver;
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

  @Override public CompletableObserver delegateObserver() {
    return delegate;
  }

  @Override public void onSubscribe(final Disposable d) {
    DisposableMaybeObserver<Object> o = new DisposableMaybeObserver<Object>() {
      @Override public void onSuccess(Object o) {
        AutoDisposableHelper.dispose(mainDisposable);
        lifecycleDisposable.lazySet(AutoDisposableHelper.DISPOSED);
      }

      @Override public void onError(Throwable e) {
        AutoDisposingCompletableObserverImpl.this.onError(e);
        mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
        lifecycleDisposable.lazySet(AutoDisposableHelper.DISPOSED);
      }

      @Override public void onComplete() {
        mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
        lifecycleDisposable.lazySet(AutoDisposableHelper.DISPOSED);
        // Noop - we're unbound now
      }
    };
    if (AutoDisposeEndConsumerHelper.setOnce(lifecycleDisposable, o, getClass())) {
      delegate.onSubscribe(this);
      lifecycle.subscribe(o);
      AutoDisposeEndConsumerHelper.setOnce(mainDisposable, d, getClass());
    }
  }

  @Override public boolean isDisposed() {
    return mainDisposable.get() == AutoDisposableHelper.DISPOSED;
  }

  @Override public void dispose() {
    AutoDisposableHelper.dispose(lifecycleDisposable);
    AutoDisposableHelper.dispose(mainDisposable);
  }

  @Override public void onComplete() {
    if (!isDisposed()) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
      delegate.onComplete();
    }
  }

  @Override public void onError(Throwable e) {
    if (!isDisposed()) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
      delegate.onError(e);
    }
  }
}
