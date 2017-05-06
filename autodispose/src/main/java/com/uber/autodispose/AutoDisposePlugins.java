/*
 * Copyright (c) 2017. Uber Technologies
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

import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;

/**
 * Utility class to inject handlers to certain standard AutoDispose operations.
 */
public final class AutoDisposePlugins {

  private AutoDisposePlugins() { }

  @Nullable
  private static volatile Consumer<? super OutsideLifecycleException> outsideLifecycleHandler;

  /**
   * Prevents changing the plugins.
   */
  static volatile boolean lockdown;

  /**
   * Prevents changing the plugins from then on.
   * <p>
   * This allows container-like environments to prevent client messing with plugins.
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
   * @return the consumer for handling {@link OutsideLifecycleException}.
   */
  @Nullable
  public static Consumer<? super OutsideLifecycleException> getOutsideLifecycleHandler() {
    return outsideLifecycleHandler;
  }

  /**
   * @param handler the consumer for handling {@link OutsideLifecycleException} to set, null allowed
   */
  public static void setOutsideLifecycleHandler(
          @Nullable Consumer<? super OutsideLifecycleException> handler) {
    if (lockdown) {
      throw new IllegalStateException("Plugins can't be changed anymore");
    }
    outsideLifecycleHandler = handler;
  }

  /**
   * Removes all handlers and resets to default behavior.
   */
  public static void reset() {
    setOutsideLifecycleHandler(null);
  }
}
