/*
 * Copyright (C) 2017. Uber Technologies
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

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.TestLifecycleScopeProvider.TestLifecycle.STARTED;
import static com.uber.autodispose.TestLifecycleScopeProvider.TestLifecycle.STOPPED;

public class TestLifecycleScopeProviderTest {

    private final TestLifecycleScopeProvider testLifecycleScopeProvider = TestLifecycleScopeProvider.create();

    @Test(expected = LifecycleNotStartedException.class) public void create_shouldReturnInUninitializedState() throws Exception {
        testLifecycleScopeProvider.correspondingEvents().apply(testLifecycleScopeProvider.peekLifecycle());
    }

    @Test public void createInitial_shouldUseInitialValuePassedIn() {
        assertThat(TestLifecycleScopeProvider.createInitial(STARTED).peekLifecycle()).isEqualTo(STARTED);
    }

    @Test public void start_shouldTriggerStartEvent() throws Exception {
        testLifecycleScopeProvider.start();

        assertThat(testLifecycleScopeProvider.peekLifecycle()).isEqualTo(STARTED);
        assertThat(testLifecycleScopeProvider.correspondingEvents().apply(testLifecycleScopeProvider.peekLifecycle()))
                .isEqualTo(STOPPED);
    }

    @Test(expected = LifecycleEndedException.class) public void stop_afterStart_shouldTriggerStopEvent() throws Exception {
        testLifecycleScopeProvider.start();
        testLifecycleScopeProvider.stop();

        assertThat(testLifecycleScopeProvider.peekLifecycle()).isEqualTo(STOPPED);
        testLifecycleScopeProvider.correspondingEvents().apply(testLifecycleScopeProvider.peekLifecycle());
    }

    @Test(expected = IllegalStateException.class) public void stop_beforeStart_shouldThrowException() throws Exception {
        testLifecycleScopeProvider.stop();
    }
}
