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

import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.subscriptions.EmptySubscription;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscription;

final class AutoDisposingSubscriberImpl<T> implements
    com.uber.autodispose.observers.AutoDisposingSubscriber<T> {

  private final AtomicReference<Subscription> mainSubscription = new AtomicReference<>();
  private final AtomicReference<Disposable> lifecycleDisposable = new AtomicReference<>();
  private final Maybe<?> lifecycle;
  private final Consumer<? super T> onNext;
  private final Consumer<? super Throwable> onError;
  private final Action onComplete;
  private final Consumer<? super Subscription> onSubscribe;

  AutoDisposingSubscriberImpl(Maybe<?> lifecycle,
      Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete,
      Consumer<? super Subscription> onSubscribe) {
    this.lifecycle = lifecycle;
    this.onError = AutoDisposeUtil.emptyErrorConsumerIfNull(onError);
    this.onNext = AutoDisposeUtil.emptyConsumerIfNull(onNext);
    this.onComplete = AutoDisposeUtil.emptyActionIfNull(onComplete);
    this.onSubscribe = AutoDisposeUtil.emptySubscriptionIfNull(onSubscribe);
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
          onSubscribe.accept(this);
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

      // If we've never actually started the upstream subscription (i.e. requested immediately in
      // onSubscribe and had a terminal event), we need to still send an empty subscription instance
      // to abide by the Subscriber contract.
      if (mainSubscription.get() == null) {
        try {
          onSubscribe.accept(EmptySubscription.INSTANCE);
        } catch (Exception e) {
          Exceptions.throwIfFatal(e);
          RxJavaPlugins.onError(e);
        }
      }
      AutoSubscriptionHelper.cancel(mainSubscription);
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
        onNext.accept(value);
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        onError(e);
      }
    }
  }

  @Override public void onError(Throwable e) {
    if (!isDisposed()) {
      cancel();
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
      cancel();
      try {
        onComplete.run();
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        RxJavaPlugins.onError(e);
      }
    }
  }
}
