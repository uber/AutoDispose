package com.uber.autodispose;

import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
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
@SuppressWarnings("ThrowableResultOfMethodCallIgnored") public class RxErrorsRule
    extends TestWatcher {

  private BlockingDeque<Throwable> errors = new LinkedBlockingDeque<>();

  @Override protected void starting(Description description) {
    RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
      @Override public void accept(Throwable t) throws Exception {
        errors.add(t);
      }
    });
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
    Throwable error;
    try {
      error = errors.pollFirst(0, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return error != null;
  }
}
