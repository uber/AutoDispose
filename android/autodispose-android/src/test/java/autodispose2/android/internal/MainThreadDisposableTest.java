/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.android.internal;

import static com.google.common.truth.Truth.assertThat;

import autodispose2.android.AutoDisposeAndroidPlugins;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class MainThreadDisposableTest {

  @Before
  @After
  public void resetPlugins() {
    AutoDisposeAndroidPlugins.reset();
  }

  @Test
  public void onDispose_defersToMainThreadHook() {
    AutoDisposeAndroidPlugins.setOnCheckMainThread(() -> true);

    final AtomicBoolean called = new AtomicBoolean();

    new MainThreadDisposable() {
      @Override
      protected void onDispose() {
        called.set(true);
      }
    }.dispose();

    assertThat(called.get()).isTrue();
  }

  @Test
  public void onDisposeFailsWhenMainThreadCheckNotSet() {
    try {
      new MainThreadDisposable() {
        @Override
        protected void onDispose() {}
      }.dispose();
      throw new AssertionError("Expected to fail before this due to Looper not being stubbed!");
    } catch (RuntimeException e) {
      // "Method myLooper in android.os.Looper not mocked..."
      // Not testing this exact message as it's an implementation detail of the test framework.
    }
  }
}
