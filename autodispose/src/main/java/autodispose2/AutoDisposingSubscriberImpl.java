/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2;

import autodispose2.observers.AutoDisposingSubscriber;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

final class AutoDisposingSubscriberImpl<@NonNull T> extends AtomicInteger
    implements AutoDisposingSubscriber<T> {

  @SuppressWarnings("WeakerAccess") // Package private for synthetic accessor saving
  final AtomicReference<Subscription> mainSubscription = new AtomicReference<>();

  @SuppressWarnings("WeakerAccess") // Package private for synthetic accessor saving
  final AtomicReference<Disposable> scopeDisposable = new AtomicReference<>();

  private final AtomicThrowable error = new AtomicThrowable();
  private final AtomicReference<Subscription> ref = new AtomicReference<>();
  private final AtomicLong requested = new AtomicLong();
  private final CompletableSource scope;
  private final Subscriber<? super T> delegate;

  AutoDisposingSubscriberImpl(CompletableSource scope, Subscriber<? super T> delegate) {
    this.scope = scope;
    this.delegate = delegate;
  }

  @Override
  public Subscriber<? super T> delegateSubscriber() {
    return delegate;
  }

  @Override
  public void onSubscribe(final Subscription s) {
    DisposableCompletableObserver o =
        new DisposableCompletableObserver() {
          @Override
          public void onError(Throwable e) {
            scopeDisposable.lazySet(AutoDisposableHelper.DISPOSED);
            AutoDisposingSubscriberImpl.this.onError(e);
          }

          @Override
          public void onComplete() {
            scopeDisposable.lazySet(AutoDisposableHelper.DISPOSED);
            AutoSubscriptionHelper.cancel(mainSubscription);
          }
        };
    if (AutoDisposeEndConsumerHelper.setOnce(scopeDisposable, o, getClass())) {
      delegate.onSubscribe(this);
      scope.subscribe(o);
      if (AutoDisposeEndConsumerHelper.setOnce(mainSubscription, s, getClass())) {
        AutoSubscriptionHelper.deferredSetOnce(ref, requested, s);
      }
    }
  }

  /**
   * Requests the specified amount from the upstream if its Subscription is set via onSubscribe
   * already.
   *
   * <p>Note that calling this method before a Subscription is set via onSubscribe leads to
   * NullPointerException and meant to be called from inside onStart or onNext.
   *
   * @param n the request amount, positive
   */
  @SuppressWarnings("NullAway")
  @Override
  public void request(long n) {
    AutoSubscriptionHelper.deferredRequest(ref, requested, n);
  }

  /**
   * Cancels the Subscription set via onSubscribe or makes sure a Subscription set asynchronously
   * (later) is cancelled immediately.
   *
   * <p>This method is thread-safe and can be exposed as a public API.
   */
  @Override
  public void cancel() {
    AutoDisposableHelper.dispose(scopeDisposable);
    AutoSubscriptionHelper.cancel(mainSubscription);
  }

  @Override
  public boolean isDisposed() {
    return mainSubscription.get() == AutoSubscriptionHelper.CANCELLED;
  }

  @Override
  public void dispose() {
    cancel();
  }

  @Override
  public void onNext(T value) {
    if (!isDisposed()) {
      if (HalfSerializer.onNext(delegate, value, this, error)) {
        // Terminal event occurred and was forwarded to the delegate, so clean up here
        mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
        AutoDisposableHelper.dispose(scopeDisposable);
      }
    }
  }

  @Override
  public void onError(Throwable e) {
    if (!isDisposed()) {
      mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
      AutoDisposableHelper.dispose(scopeDisposable);
      HalfSerializer.onError(delegate, e, this, error);
    }
  }

  @Override
  public void onComplete() {
    if (!isDisposed()) {
      mainSubscription.lazySet(AutoSubscriptionHelper.CANCELLED);
      AutoDisposableHelper.dispose(scopeDisposable);
      HalfSerializer.onComplete(delegate, this, error);
    }
  }
}
