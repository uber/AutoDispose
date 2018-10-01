/*
 * Copyright (C) 2018. Uber Technologies
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

import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static com.google.common.truth.Truth.assertThat;

/**
 * JUnit rule to record RxJava errors.
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class RxErrorsRule extends TestWatcher {

  private BlockingDeque<Throwable> errors = new LinkedBlockingDeque<>();

  @Override protected void starting(Description description) {
    RxJavaPlugins.setErrorHandler(t -> errors.add(t));
  }

  @Override protected void finished(Description description) {
    RxJavaPlugins.setErrorHandler(null);
  }

  public List<Throwable> getErrors() {
    return new ArrayList<>(errors);
  }

  public Throwable take() {
    Throwable error;
    try {
      error = errors.pollFirst(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (error == null) {
      throw new NoSuchElementException("No errors recorded.");
    }
    return error;
  }

  public Throwable takeThrowableFromUndeliverableException() {
    Throwable error = take();
    assertThat(error).isInstanceOf(UndeliverableException.class);
    return error.getCause();
  }

  public CompositeException takeCompositeException() {
    Throwable error = take();
    assertThat(error).isInstanceOf(CompositeException.class);
    return (CompositeException) error;
  }

  public boolean hasErrors() {
    Throwable error = errors.peek();
    return error != null;
  }

  public void assertNoErrors() {
    if (hasErrors()) {
      throw new AssertionError("Expected no errors but found " + getErrors());
    }
  }
}
