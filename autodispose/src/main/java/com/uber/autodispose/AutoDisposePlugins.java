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
package com.uber.autodispose;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.functions.Consumer;

/** Utility class to inject handlers to certain standard autodispose-lifecycle operations. */
public final class AutoDisposePlugins {

  private AutoDisposePlugins() {}

  @Nullable private static volatile Consumer<? super OutsideScopeException> outsideScopeHandler;
  static volatile boolean fillInOutsideScopeExceptionStacktraces;
  static volatile boolean hideProxies = true;

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
   * @return the value indicating whether or not to hide proxy interfaces.
   * @see #setHideProxies(boolean)
   */
  public static boolean getHideProxies() {
    return hideProxies;
  }

  /**
   * @return the value indicating whether or not to fill in stacktraces in {@link
   *     OutsideScopeException}.
   */
  public static boolean getFillInOutsideScopeExceptionStacktraces() {
    return fillInOutsideScopeExceptionStacktraces;
  }

  /** @return the value for handling {@link OutsideScopeException}. */
  @Nullable
  public static Consumer<? super OutsideScopeException> getOutsideScopeHandler() {
    return outsideScopeHandler;
  }

  /** @param handler the consumer for handling {@link OutsideScopeException} to set, null allowed */
  public static void setOutsideScopeHandler(
      @Nullable Consumer<? super OutsideScopeException> handler) {
    if (lockdown) {
      throw new IllegalStateException("Plugins can't be changed anymore");
    }
    outsideScopeHandler = handler;
  }

  /**
   * @param fillInStacktrace {@code true} to fill in stacktraces in {@link OutsideScopeException}s.
   *     {@code false} to disable them (and use them as signals only). Disabling them, if you don't
   *     care about the stacktraces, can result in some minor performance improvements.
   */
  public static void setFillInOutsideScopeExceptionStacktraces(boolean fillInStacktrace) {
    if (lockdown) {
      throw new IllegalStateException("Plugins can't be changed anymore");
    }
    fillInOutsideScopeExceptionStacktraces = fillInStacktrace;
  }

  /**
   * @param hideProxies {@code true} hide proxy interfaces. This wraps all proxy interfaces in
   *     {@link com.uber.autodispose} at runtime in an anonymous instance to prevent introspection,
   *     similar to {@link Observable#hide()}. The default is {@code true}.
   */
  public static void setHideProxies(boolean hideProxies) {
    if (lockdown) {
      throw new IllegalStateException("Plugins can't be changed anymore");
    }
    AutoDisposePlugins.hideProxies = hideProxies;
  }

  /** Removes all handlers and resets to default behavior. */
  public static void reset() {
    setOutsideScopeHandler(null);
  }
}
