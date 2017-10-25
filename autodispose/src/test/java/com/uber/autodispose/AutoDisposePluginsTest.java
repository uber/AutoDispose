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

import org.junit.After;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class AutoDisposePluginsTest {

  @After public void tearDown() {
    AutoDisposePlugins.reset();
  }

  @Test public void noStacktraceFill_shouldHaveNoStacktrace() {
    AutoDisposePlugins.setFillInOutsideLifecycleExceptionStacktraces(false);

    LifecycleNotStartedException started =
        new LifecycleNotStartedException("Lifecycle not started");
    assertThat(started.getStackTrace()).isEmpty();

    LifecycleEndedException ended = new LifecycleEndedException("Lifecycle ended");
    assertThat(ended.getStackTrace()).isEmpty();
  }

  @Test public void defaultStacktraceFill_shouldHaveStacktrace() {
    LifecycleNotStartedException started =
        new LifecycleNotStartedException("Lifecycle not started");
    assertThat(started.getStackTrace()).isNotEmpty();

    LifecycleEndedException ended = new LifecycleEndedException("Lifecycle ended");
    assertThat(ended.getStackTrace()).isNotEmpty();
  }

  @Test public void trueStacktraceFill_shouldHaveStacktrace() {
    AutoDisposePlugins.setFillInOutsideLifecycleExceptionStacktraces(true);

    LifecycleNotStartedException started =
        new LifecycleNotStartedException("Lifecycle not started");
    assertThat(started.getStackTrace()).isNotEmpty();

    LifecycleEndedException ended = new LifecycleEndedException("Lifecycle ended");
    assertThat(ended.getStackTrace()).isNotEmpty();
  }
}
