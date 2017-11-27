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
import io.reactivex.observers.DisposableMaybeObserver;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

final class AutoDisposingSubscriberImpl<T> extends AtomicInteger
    implements AutoDisposingSubscriber<T> {

  private final AtomicReference<Subscription> mainSubscription = new AtomicReference<>();
  private final AtomicReference<Disposable> lifecycleDisposable = new AtomicReference<>();
  private final AtomicThrowable error = new AtomicThrowable();
  private final AtomicReference<Subscription> ref = new AtomicReference<>();
  private final AtomicLong requested = new AtomicLong();
  private final Maybe<?> lifecycle;
  private final Subscriber<? super T> delegate;

  AutoDisposingSubscriberImpl(Maybe<?> lifecycle, Subscriber<? super T> delegate) {
    this.lifecycle = lifecycle;
    this.delegate = delegate;
  }

  @Override public Subscriber<? super T> delegateSubscriber() {
    return delegate;
  }

  @Override public void onSubscribe(final Subscription s) {
    DisposableMaybeObserver<Object> o = new DisposableMaybeObserver<Object>() {
      @Override public void onSuccess(Object o) {
        lifecycleDisposable.lazySet(AutoDisposableHelper.DISPOSED);
        AutoSubscriptionHelper.cancel(mainSubscription);
      }

      @Override public void onError(Throwable e) {
        AutoDisposingSubscriberImpl.this.onError(e);
        mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
        lifecycleDisposable.lazySet(AutoDisposableHelper.DISPOSED);
      }

      @Override public void onComplete() {
        mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
        lifecycleDisposable.lazySet(AutoDisposableHelper.DISPOSED);
        // Noop - we're unbound now
      }
    };
    if (AutoDisposeEndConsumerHelper.setOnce(lifecycleDisposable, o, getClass())) {
      delegate.onSubscribe(this);
      lifecycle.subscribe(o);
      if (AutoDisposeEndConsumerHelper.setOnce(mainSubscription, s, getClass())) {
        AutoSubscriptionHelper.deferredSetOnce(ref, requested, s);
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
  @SuppressWarnings("NullAway") @Override public void request(long n) {
    AutoSubscriptionHelper.deferredRequest(ref, requested, n);
  }

  /**
   * Cancels the Subscription set via onSubscribe or makes sure a
   * Subscription set asynchronously (later) is cancelled immediately.
   * <p>This method is thread-safe and can be exposed as a public API.
   */
  @Override public void cancel() {
    AutoDisposableHelper.dispose(lifecycleDisposable);
    AutoSubscriptionHelper.cancel(mainSubscription);
  }

  @Override public boolean isDisposed() {
    return mainSubscription.get() == AutoSubscriptionHelper.CANCELLED;
  }

  @Override public void dispose() {
    cancel();
  }

  @Override public void onNext(T value) {
    if (!isDisposed()) {
      if (HalfSerializer.onNext(delegate, value, this, error)) {
        // Terminal event occurred and was forwarded to the delegate, so clean up here
        mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
        AutoDisposableHelper.dispose(lifecycleDisposable);
      }
    }
  }

  @Override public void onError(Throwable e) {
    if (!isDisposed()) {
      mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
      AutoDisposableHelper.dispose(lifecycleDisposable);
      HalfSerializer.onError(delegate, e, this, error);
    }
  }

  @Override public void onComplete() {
    if (!isDisposed()) {
      mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
      AutoDisposableHelper.dispose(lifecycleDisposable);
      HalfSerializer.onComplete(delegate, this, error);
    }
  }
}
