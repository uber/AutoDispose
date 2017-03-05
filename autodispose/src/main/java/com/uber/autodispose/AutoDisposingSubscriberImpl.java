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

import com.uber.autodispose.observers.AutoDisposingSubscriber;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.subscriptions.EmptySubscription;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

final class AutoDisposingSubscriberImpl<T> implements AutoDisposingSubscriber<T> {

  private final AtomicReference<Subscription> mainSubscription = new AtomicReference<>();
  private final AtomicReference<Disposable> lifecycleDisposable = new AtomicReference<>();
  private final Maybe<?> lifecycle;
  private final Subscriber<? super T> delegate;

  AutoDisposingSubscriberImpl(Maybe<?> lifecycle, Subscriber<? super T> delegate) {
    this.lifecycle = lifecycle;
    this.delegate = delegate;
  }

  @Override public final void onSubscribe(Subscription s) {
    if (AutoDisposableHelper.setOnce(lifecycleDisposable,
        lifecycle.subscribe(new Consumer<Object>() {
          @Override public void accept(Object o) throws Exception {
            dispose();
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable e) throws Exception {
            AutoDisposingSubscriberImpl.this.onError(e);
          }
        }))) {
      if (AutoSubscriptionHelper.setOnce(mainSubscription, s)) {
        try {
          delegate.onSubscribe(this);
        } catch (Throwable t) {
          Exceptions.throwIfFatal(t);
          s.cancel();
          onError(t);
        }
      }
    }
  }

  /**
   * Requests the specified amount from the upstream if its Subscription is set via
   * onSubscribe already.
   * <p>Note that calling this method before a Subscription is set via onSubscribe
   * leads to NullPointerException and meant to be called from inside onStart or
   * onNext.
   *
   * @param n the request amount, positive
   */
  @Override public final void request(long n) {
    mainSubscription.get()
        .request(n);
  }

  /**
   * Cancels the Subscription set via onSubscribe or makes sure a
   * Subscription set asynchronously (later) is cancelled immediately.
   * <p>This method is thread-safe and can be exposed as a public API.
   */
  @Override public final void cancel() {
    synchronized (this) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      callMainSubscribeIfNecessary();
      AutoSubscriptionHelper.cancel(mainSubscription);
    }
  }

  private void lazyCancel() {
    synchronized (this) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      callMainSubscribeIfNecessary();
      mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
    }
  }

  private void callMainSubscribeIfNecessary() {
    // If we've never actually started the upstream subscription (i.e. requested immediately in
    // onSubscribe and had a terminal event), we need to still send an empty subscription instance
    // to abide by the Subscriber contract.
    if (mainSubscription.get() == null) {
      try {
        delegate.onSubscribe(EmptySubscription.INSTANCE);
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        RxJavaPlugins.onError(e);
      }
    }
  }

  @Override public final boolean isDisposed() {
    return mainSubscription.get() == AutoSubscriptionHelper.CANCELLED;
  }

  @Override public final void dispose() {
    cancel();
  }

  @Override public final void onNext(T value) {
    if (!isDisposed()) {
      try {
        delegate.onNext(value);
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        onError(e);
      }
    }
  }

  @Override public void onError(Throwable e) {
    if (!isDisposed()) {
      lazyCancel();
      try {
        delegate.onError(e);
      } catch (Exception e1) {
        Exceptions.throwIfFatal(e1);
        RxJavaPlugins.onError(new CompositeException(e, e1));
      }
    }
  }

  @Override public final void onComplete() {
    if (!isDisposed()) {
      lazyCancel();
      try {
        delegate.onComplete();
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        RxJavaPlugins.onError(e);
      }
    }
  }
}
