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

import com.uber.autodispose.observers.AutoDisposingObserver;
import io.reactivex.Maybe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import java.util.concurrent.atomic.AtomicReference;

final class AutoDisposingObserverImpl<T> implements AutoDisposingObserver<T> {

  private final AtomicReference<Disposable> mainDisposable = new AtomicReference<>();
  private final AtomicReference<Disposable> lifecycleDisposable = new AtomicReference<>();
  private final Maybe<?> lifecycle;
  private final Observer<? super T> delegate;

  AutoDisposingObserverImpl(Maybe<?> lifecycle, Observer<? super T> delegate) {
    this.lifecycle = lifecycle;
    this.delegate = delegate;
  }

  @Override public Observer<? super T> delegateObserver() {
    return delegate;
  }

  @Override public void onSubscribe(final Disposable d) {
    if (AutoDisposeEndConsumerHelper.setOnce(lifecycleDisposable,
        lifecycle.doOnEvent(new BiConsumer<Object, Throwable>() {
          @Override public void accept(Object o, Throwable throwable) throws Exception {
            callMainSubscribeIfNecessary(d);
          }
        })
            .subscribe(new Consumer<Object>() {
              @Override public void accept(Object o) throws Exception {
                dispose();
              }
            }, new Consumer<Throwable>() {
              @Override public void accept(Throwable e) throws Exception {
                AutoDisposingObserverImpl.this.onError(e);
              }
            }), getClass())) {
      if (AutoDisposeEndConsumerHelper.setOnce(mainDisposable, d, getClass())) {
        delegate.onSubscribe(this);
      }
    }
  }

  @Override public boolean isDisposed() {
    return mainDisposable.get() == AutoDisposableHelper.DISPOSED;
  }

  @Override public void dispose() {
    synchronized (this) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      AutoDisposableHelper.dispose(mainDisposable);
    }
  }

  private void lazyDispose() {
    synchronized (this) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      mainDisposable.lazySet(AutoDisposableHelper.DISPOSED);
    }
  }

  @SuppressWarnings("WeakerAccess") // Avoiding synthetic accessors
  void callMainSubscribeIfNecessary(Disposable d) {
    // If we've never actually called the downstream onSubscribe (i.e. requested immediately in
    // onSubscribe and had a terminal event), we need to still send an empty disposable instance
    // to abide by the Observer contract.
    if (AutoDisposableHelper.setIfNotSet(mainDisposable, d)) {
      delegate.onSubscribe(Disposables.disposed());
    }
  }

  @Override public void onNext(T value) {
    if (!isDisposed()) {
      delegate.onNext(value);
    }
  }

  @Override public void onError(Throwable e) {
    if (!isDisposed()) {
      lazyDispose();
      delegate.onError(e);
    }
  }

  @Override public void onComplete() {
    if (!isDisposed()) {
      lazyDispose();
      delegate.onComplete();
    }
  }
}
