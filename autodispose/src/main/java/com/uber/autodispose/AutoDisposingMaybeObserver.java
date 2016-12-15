package com.uber.autodispose;

import com.uber.autodispose.internal.AutoDisposableHelper;
import com.uber.autodispose.internal.AutoDisposeUtil;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
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

  AutoDisposingMaybeObserver(Maybe<?> lifecycle,
      Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError,
      Action onComplete,
      Consumer<? super Disposable> onSubscribe) {
    this.lifecycle = lifecycle;
    this.onSuccess = AutoDisposeUtil.emptyConsumerIfNull(onSuccess);
    this.onError = AutoDisposeUtil.emptyErrorConsumerIfNull(onError);
    this.onComplete = AutoDisposeUtil.emptyActionIfNull(onComplete);
    this.onSubscribe = AutoDisposeUtil.emptyDisposableIfNull(onSubscribe);
  }

  @Override
  public final void onSubscribe(Disposable d) {
    if (AutoDisposableHelper.setOnce(this.mainDisposable, d)) {
      AutoDisposableHelper.setOnce(this.lifecycleDisposable,
          lifecycle.subscribe(e -> dispose(), this::onError));
      try {
        onSubscribe.accept(this);
      } catch (Throwable t) {
        Exceptions.throwIfFatal(t);
        d.dispose();
        onError(t);
      }
    }
  }

  @Override
  public final boolean isDisposed() {
    return mainDisposable.get() == AutoDisposableHelper.DISPOSED;
  }

  @Override
  public final void dispose() {
    synchronized (this) {
      AutoDisposableHelper.dispose(lifecycleDisposable);
      AutoDisposableHelper.dispose(mainDisposable);
    }
  }

  @Override
  public final void onSuccess(T value) {
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

  @Override
  public final void onError(Throwable e) {
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

  @Override
  public final void onComplete() {
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
