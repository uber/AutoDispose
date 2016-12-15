package com.uber.autodispose;

import com.uber.autodispose.internal.AutoDisposableHelper;
import com.uber.autodispose.internal.AutoDisposeUtil;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicReference;

public final class AutoDisposingCompletableObserver implements CompletableObserver, Disposable {

  private final AtomicReference<Disposable> mainDisposable = new AtomicReference<>();
  private final AtomicReference<Disposable> lifecycleDisposable = new AtomicReference<>();
  private final Maybe<?> lifecycle;
  private final Consumer<? super Throwable> onError;
  private final Action onComplete;
  private final Consumer<? super Disposable> onSubscribe;

  AutoDisposingCompletableObserver(Maybe<?> lifecycle,
      Action onComplete,
      Consumer<? super Throwable> onError,
      Consumer<? super Disposable> onSubscribe) {
    this.lifecycle = lifecycle;
    this.onComplete = AutoDisposeUtil.emptyActionIfNull(onComplete);
    this.onError = AutoDisposeUtil.emptyErrorConsumerIfNull(onError);
    this.onSubscribe = AutoDisposeUtil.emptyDisposableIfNull(onSubscribe);
  }

  @Override
  public final void onSubscribe(Disposable d) {
    if (AutoDisposableHelper.setOnce(mainDisposable, d)) {
      AutoDisposableHelper.setOnce(lifecycleDisposable,
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
}
