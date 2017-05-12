/**
 * Copyright 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.uber.autodispose;

import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * Utility methods for working with Disposables atomically. Copied from the RxJava implementation.
 */
enum AutoDisposableHelper implements Disposable {
  /**
   * The singleton instance representing a terminal, disposed state, don't leak it.
   */
  DISPOSED;

  static boolean isDisposed(Disposable d) {
    return d == DISPOSED;
  }

  static boolean set(AtomicReference<Disposable> field, @Nullable Disposable d) {
    for (; ; ) {
      Disposable current = field.get();
      if (current == DISPOSED) {
        if (d != null) {
          d.dispose();
        }
        return false;
      }
      if (field.compareAndSet(current, d)) {
        if (current != null) {
          current.dispose();
        }
        return true;
      }
    }
  }

  /**
   * Atomically sets the field to the given non-null Disposable and returns true
   * or returns false if the field is non-null.
   * If the target field contains the common DISPOSED instance, the supplied disposable
   * is disposed. If the field contains other non-null Disposable, an IllegalStateException
   * is signalled to the RxJavaPlugins.onError hook.
   *
   * @param field the target field
   * @param d the disposable to set, not null
   * @return true if the operation succeeded, false
   */
  static boolean setOnce(AtomicReference<Disposable> field, Disposable d) {
    AutoDisposeUtil.checkNotNull(d, "d is null");
    if (!field.compareAndSet(null, d)) {
      d.dispose();
      if (field.get() != DISPOSED) {
        reportDisposableSet();
      }
      return false;
    }
    return true;
  }

  /**
   * Atomically sets the field to the given non-null Disposable and returns true
   * or returns false if the field is non-null.
   *
   * @param field the target field
   * @param d the disposable to set, not null
   * @return true if the operation succeeded, false
   */
  static boolean setIfNotSet(AtomicReference<Disposable> field, Disposable d) {
    AutoDisposeUtil.checkNotNull(d, "d is null");
    return field.compareAndSet(null, d);
  }

  /**
   * Atomically replaces the Disposable in the field with the given new Disposable
   * but does not dispose the old one.
   *
   * @param field the target field to change
   * @param d the new disposable, null allowed
   * @return true if the operation succeeded, false if the target field contained
   * the common DISPOSED instance and the given disposable (if not null) is disposed.
   */
  static boolean replace(AtomicReference<Disposable> field, @Nullable Disposable d) {
    for (; ; ) {
      Disposable current = field.get();
      if (current == DISPOSED) {
        if (d != null) {
          d.dispose();
        }
        return false;
      }
      if (field.compareAndSet(current, d)) {
        return true;
      }
    }
  }

  /**
   * Atomically disposes the Disposable in the field if not already disposed.
   *
   * @param field the target field
   * @return true if the current thread managed to dispose the Disposable
   */
  static boolean dispose(AtomicReference<Disposable> field) {
    Disposable current = field.get();
    Disposable d = DISPOSED;
    if (current != d) {
      current = field.getAndSet(d);
      if (current != d) {
        if (current != null) {
          current.dispose();
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Verifies that current is null, next is not null, otherwise signals errors
   * to the RxJavaPlugins and returns false.
   *
   * @param current the current Disposable, expected to be null
   * @param next the next Disposable, expected to be non-null
   * @return true if the validation succeeded
   */
  static boolean validate(@Nullable Disposable current, Disposable next) {
    //noinspection ConstantConditions leftover from original RxJava implementation
    if (next == null) {
      RxJavaPlugins.onError(new NullPointerException("next is null"));
      return false;
    }
    if (current != null) {
      next.dispose();
      reportDisposableSet();
      return false;
    }
    return true;
  }

  /**
   * Reports that the disposable is already set to the RxJavaPlugins error handler.
   */
  static void reportDisposableSet() {
    RxJavaPlugins.onError(new IllegalStateException("Disposable already set!"));
  }

  @Override public void dispose() {
    // deliberately no-op
  }

  @Override public boolean isDisposed() {
    return true;
  }
}
