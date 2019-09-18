/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.autodispose.test;

import static com.google.common.truth.Truth.assertThat;

import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public final class RecordingObserver<T>
    implements Observer<T>, SingleObserver<T>, MaybeObserver<T>, CompletableObserver {

  public interface Logger {
    void log(String message);
  }

  private final BlockingDeque<Object> events = new LinkedBlockingDeque<>();
  private final Logger logger;

  public RecordingObserver(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void onError(Throwable e) {
    logger.log("onError - " + e);
    events.addLast(new OnError(e));
  }

  @Override
  public void onComplete() {
    logger.log("onCompleted");
    events.addLast(new OnCompleted());
  }

  @Override
  public void onSubscribe(Disposable d) {
    logger.log("onSubscribe");
    events.addLast(new OnSubscribe(d));
  }

  @Override
  public void onSuccess(T value) {
    logger.log("onSuccess - " + value);
    events.addLast(new OnSuccess(value));
  }

  @Override
  public void onNext(T t) {
    logger.log("onNext - " + t);
    events.addLast(new OnNext(t));
  }

  private <E> E takeEvent(Class<E> wanted) {
    Object event;
    try {
      event = events.pollFirst(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (event == null) {
      throw new NoSuchElementException(
          "No event found while waiting for " + wanted.getSimpleName());
    }
    assertThat(event).isInstanceOf(wanted);
    return wanted.cast(event);
  }

  public boolean hasNextEvent() {
    Object event;
    try {
      event = events.pollFirst(0, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return event != null && OnNext.class.isInstance(event);
  }

  public T takeNext() {
    OnNext event = takeEvent(OnNext.class);
    return event.value;
  }

  public T takeSuccess() {
    OnSuccess event = takeEvent(OnSuccess.class);
    return event.value;
  }

  public Disposable takeSubscribe() {
    return takeEvent(OnSubscribe.class).disposable;
  }

  public Throwable takeError() {
    return takeEvent(OnError.class).throwable;
  }

  public void assertOnComplete() {
    takeEvent(OnCompleted.class);
  }

  public void assertNoMoreEvents() {
    try {
      Object event = takeEvent(Object.class);
      throw new IllegalStateException("Expected no more events but got " + event);
    } catch (NoSuchElementException ignored) {
    }
  }

  private final class OnNext {
    final T value;

    /* private */
    OnNext(T value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return "OnNext[" + value + "]";
    }
  }

  private static final class OnCompleted {
    @Override
    public String toString() {
      return "OnCompleted";
    }
  }

  private static final class OnError {
    /* private */
    final Throwable throwable;

    /* private */
    OnError(Throwable throwable) {
      this.throwable = throwable;
    }

    @Override
    public String toString() {
      return "OnError[" + throwable + "]";
    }
  }

  private static final class OnSubscribe {
    /* private */
    final Disposable disposable;

    /* private */
    OnSubscribe(Disposable disposable) {
      this.disposable = disposable;
    }

    @Override
    public String toString() {
      return "OnSubscribe";
    }
  }

  private final class OnSuccess {
    /* private */
    final T value;

    /* private */
    OnSuccess(T value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return "OnSuccess[" + value + "]";
    }
  }
}
