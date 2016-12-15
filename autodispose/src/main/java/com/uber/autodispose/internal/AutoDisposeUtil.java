package com.uber.autodispose.internal;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import javax.annotation.Nullable;
import org.reactivestreams.Subscription;

public class AutoDisposeUtil {

  private AutoDisposeUtil() {
    throw new InstantiationError();
  }

  public static final Action EMPTY_ACTION = new Action() {
    @Override
    public void run() { }

    @Override
    public String toString() {
      return "AutoDisposingEmptyAction";
    }
  };

  public static final Consumer<Object> EMPTY_CONSUMER = new Consumer<Object>() {
    @Override
    public void accept(Object v) { }

    @Override
    public String toString() {
      return "AutoDisposingEmptyConsumer";
    }
  };

  public static final Consumer<Throwable> DEFAULT_ERROR_CONSUMER = new Consumer<Throwable>() {
    @Override
    public void accept(Throwable throwable) throws Exception { }

    @Override
    public String toString() {
      return "AutoDisposingEmptyErrorConsumer";
    }
  };

  public static final Consumer<Disposable> EMPTY_DISPOSABLE_CONSUMER = new Consumer<Disposable>() {
    @Override
    public void accept(Disposable d) throws Exception { }

    @Override
    public String toString() {
      return "AutoDisposingEmptyDisposableConsumer";
    }
  };

  public static final Consumer<Subscription> EMPTY_SUBSCRIPTION_CONSUMER =
      new Consumer<Subscription>() {
        @Override
        public void accept(Subscription d) throws Exception {
        }

        @Override
        public String toString() {
          return "AutoDisposingEmptySubscriptionConsumer";
        }
      };

  @SuppressWarnings("unchecked")
  public static <T> Consumer<T> emptyConsumerIfNull(@Nullable Consumer<T> c) {
    return c != null ? c : (Consumer<T>) EMPTY_CONSUMER;
  }

  public static Consumer<? super Throwable> emptyErrorConsumerIfNull(
      @Nullable Consumer<? super Throwable> c) {
    return c != null ? c : DEFAULT_ERROR_CONSUMER;
  }

  public static Consumer<? super Disposable> emptyDisposableIfNull(
      @Nullable Consumer<? super Disposable> c) {
    return c != null ? c : EMPTY_DISPOSABLE_CONSUMER;
  }

  public static Consumer<? super Subscription> emptySubscriptionIfNull(
      @Nullable Consumer<? super Subscription> c) {
    return c != null ? c : EMPTY_SUBSCRIPTION_CONSUMER;
  }

  public static Action emptyActionIfNull(@Nullable Action a) {
    return a != null ? a : EMPTY_ACTION;
  }

  public static <T> T checkNotNull(@Nullable T value, String message) {
    if (value == null) {
      throw new NullPointerException(message);
    } else {
      return value;
    }
  }
}
