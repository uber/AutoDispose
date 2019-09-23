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
package autodispose2.android;

import androidx.annotation.Nullable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.BooleanSupplier;

/** Utility class to inject handlers to certain standard AutoDispose Android operations. */
public final class AutoDisposeAndroidPlugins {

  private AutoDisposeAndroidPlugins() {}

  @Nullable private static volatile BooleanSupplier onCheckMainThread;

  /** Prevents changing the plugins. */
  static volatile boolean lockdown;

  /**
   * Prevents changing the plugins from then on.
   *
   * <p>This allows container-like environments to prevent client messing with plugins.
   */
  public static void lockdown() {
    lockdown = true;
  }

  /**
   * Returns true if the plugins were locked down.
   *
   * @return true if the plugins were locked down
   */
  public static boolean isLockdown() {
    return lockdown;
  }

  /**
   * Sets the preferred main thread checker. If not {@code null}, the {@code mainThreadChecker} will
   * be preferred in all main thread checks in {@link #onCheckMainThread(BooleanSupplier)} calls.
   * This can be useful for JVM testing environments, where standard Android Looper APIs cannot be
   * stubbed and thus should be overridden with a custom check.
   *
   * <p>This is a reset-able API, which means you can pass {@code null} as the parameter value to
   * reset it. Alternatively, you can call {@link #reset()}.
   *
   * @param mainThreadChecker a {@link BooleanSupplier} to call to check if current execution is on
   *     the main thread. Should return {@code true} if it is on the main thread or {@code false} if
   *     not.
   */
  public static void setOnCheckMainThread(@Nullable BooleanSupplier mainThreadChecker) {
    if (lockdown) {
      throw new IllegalStateException("Plugins can't be changed anymore");
    }
    onCheckMainThread = mainThreadChecker;
  }

  /**
   * Returns {@code true} if called on the main thread, {@code false} if not. This will prefer a set
   * checker via {@link #setOnCheckMainThread(BooleanSupplier)} if one is present, otherwise it will
   * use {@code defaultChecker}.
   *
   * @param defaultChecker the default checker to fall back to if there is no main thread checker
   *     set.
   * @return {@code true} if called on the main thread, {@code false} if not.
   */
  public static boolean onCheckMainThread(BooleanSupplier defaultChecker) {
    if (defaultChecker == null) {
      throw new NullPointerException("defaultChecker == null");
    }
    BooleanSupplier current = onCheckMainThread;
    try {
      if (current == null) {
        return defaultChecker.getAsBoolean();
      } else {
        return current.getAsBoolean();
      }
    } catch (Throwable ex) {
      throw Exceptions.propagate(ex);
    }
  }

  /** Removes all handlers and resets to default behavior. */
  public static void reset() {
    setOnCheckMainThread(null);
  }
}
