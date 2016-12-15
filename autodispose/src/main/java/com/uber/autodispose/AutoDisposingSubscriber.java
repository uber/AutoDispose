package com.uber.autodispose;

import com.uber.autodispose.internal.AutoDisposableHelper;
import com.uber.autodispose.internal.AutoDisposeUtil;
import com.uber.autodispose.internal.AutoSubscriptionHelper;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public final class AutoDisposingSubscriber<T> implements Subscriber<T>, Subscription, Disposable {

  private final AtomicReference<Subscription> mainSubscription = new AtomicReference<>();
  private final AtomicReference<Disposable> lifecycleDisposable = new AtomicReference<>();
  private final Maybe<?> lifecycle;
  private final Consumer<? super T> onNext;
  private final Consumer<? super Throwable> onError;
  private final Action onComplete;
  private final Consumer<? super Subscription> onSubscribe;

  AutoDisposingSubscriber(Maybe<?> lifecycle,
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

  @Override
  public final void onSubscribe(Subscription s) {
    if (AutoSubscriptionHelper.setOnce(mainSubscription, s)) {
      AutoDisposableHelper.setOnce(lifecycleDisposable,
          lifecycle.subscribe(e -> cancel(), this::onError));
      try {
        onSubscribe.accept(this);
      } catch (Throwable t) {
        Exceptions.throwIfFatal(t);
        s.cancel();
        onError(t);
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
  @Override
  public final void request(long n) {
    mainSubscription.get()
        .request(n);
  }

  /**
   * Cancels the Subscription set via onSubscribe or makes sure a
   * Subscription set asynchronously (later) is cancelled immediately.
   * <p>This method is thread-safe and can be exposed as a public API.
   */
  @Override
  public final void cancel() {
    synchronized (this) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      AutoSubscriptionHelper.cancel(mainSubscription);
    }
  }

  @Override
  public final boolean isDisposed() {
    return mainSubscription.get() == AutoSubscriptionHelper.CANCELLED;
  }

  @Override
  public final void dispose() {
    cancel();
  }

  @Override
  public final void onNext(T value) {
    if (!isDisposed()) {
      try {
        onNext.accept(value);
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        onError(e);
      }
    }
  }

  @Override
  public void onError(Throwable e) {
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

  @Override
  public final void onComplete() {
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
