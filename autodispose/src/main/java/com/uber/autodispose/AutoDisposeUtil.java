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

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import javax.annotation.Nullable;
import org.reactivestreams.Subscription;

final class AutoDisposeUtil {

  private AutoDisposeUtil() {
    throw new InstantiationError();
  }

  static final Action EMPTY_ACTION = new Action() {
    @Override public void run() {}

    @Override public String toString() {
      return "AutoDisposingEmptyAction";
    }
  };

  static final Consumer<Object> EMPTY_CONSUMER = new Consumer<Object>() {
    @Override public void accept(Object v) {}

    @Override public String toString() {
      return "AutoDisposingEmptyConsumer";
    }
  };

  static final Consumer<Throwable> DEFAULT_ERROR_CONSUMER = new Consumer<Throwable>() {
    @Override public void accept(Throwable throwable) throws Exception {}

    @Override public String toString() {
      return "AutoDisposingEmptyErrorConsumer";
    }
  };

  static final Consumer<Disposable> EMPTY_DISPOSABLE_CONSUMER = new Consumer<Disposable>() {
    @Override public void accept(Disposable d) throws Exception {}

    @Override public String toString() {
      return "AutoDisposingEmptyDisposableConsumer";
    }
  };

  static final Consumer<Subscription> EMPTY_SUBSCRIPTION_CONSUMER = new Consumer<Subscription>() {
    @Override public void accept(Subscription d) throws Exception {
    }

    @Override public String toString() {
      return "AutoDisposingEmptySubscriptionConsumer";
    }
  };

  @SuppressWarnings("unchecked")
  static <T> Consumer<T> emptyConsumerIfNull(@Nullable Consumer<T> c) {
    return c != null ? c : (Consumer<T>) EMPTY_CONSUMER;
  }

  @SuppressWarnings("unchecked") static Consumer<? super Throwable> emptyErrorConsumerIfNull(
      @Nullable Consumer<? super Throwable> c) {
    return (Consumer<? super Throwable>) (c != null ? c : DEFAULT_ERROR_CONSUMER);
  }

  @SuppressWarnings("unchecked") static Consumer<? super Disposable> emptyDisposableIfNull(
      @Nullable Consumer<? super Disposable> c) {
    return (Consumer<? super Disposable>) (c != null ? c : EMPTY_DISPOSABLE_CONSUMER);
  }

  @SuppressWarnings("unchecked") static Consumer<? super Subscription> emptySubscriptionIfNull(
      @Nullable Consumer<? super Subscription> c) {
    return (Consumer<? super Subscription>) (c != null ? c : EMPTY_SUBSCRIPTION_CONSUMER);
  }

  static Action emptyActionIfNull(@Nullable Action a) {
    return a != null ? a : EMPTY_ACTION;
  }

  static <T> T checkNotNull(@Nullable T value, String message) {
    if (value == null) {
      throw new NullPointerException(message);
    } else {
      return value;
    }
  }
}
