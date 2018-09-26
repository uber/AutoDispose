/*
 * Copyright (c) 2018. Uber Technologies
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

package com.uber.autodispose.android;

import com.uber.autodispose.AutoDisposePlugins;
import com.uber.autodispose.android.internal.AutoDisposeAndroidUtil;
import io.reactivex.functions.BooleanSupplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class AutoDisposeAndroidPluginsTest {

  @Before @After public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test public void overridingMainThreadCheck_shouldWorkInUnitTests() {
    expectLooperError();

    AutoDisposeAndroidPlugins.setOnCheckMainThread(() -> true);

    assertThat(AutoDisposeAndroidUtil.isMainThread()).isTrue();

    AutoDisposeAndroidPlugins.reset();

    AutoDisposeAndroidPlugins.setOnCheckMainThread(() -> false);

    assertThat(AutoDisposeAndroidUtil.isMainThread()).isFalse();

    // Now reset and confirm we're back to normal
    AutoDisposeAndroidPlugins.setOnCheckMainThread(null);
    expectLooperError();
  }

  private void expectLooperError() {
    // Default case definitely hits the Looper code.
    // This does not work in standard Android unit tests.
    try {
      AutoDisposeAndroidUtil.isMainThread();
      throw new AssertionError("Expected to fail before this due to Looper not being stubbed!");
    } catch (Exception e) {
      // "Method myLooper in android.os.Looper not mocked..."
      // Not testing this exact message as it's an implementation detail of the test framework.
    }
  }
}
