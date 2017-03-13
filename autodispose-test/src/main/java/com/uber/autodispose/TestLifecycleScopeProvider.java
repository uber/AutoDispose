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

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Test utility to create {@link LifecycleScopeProvider} instances for tests.
 *
 * Supports a start and stop lifecycle. Subscribing when outside of the lifecycle will throw either a
 * {@link LifecycleNotStartedException} or {@link LifecycleEndedException}.
 * }
 */
public final class TestLifecycleScopeProvider implements LifecycleScopeProvider {

    private final BehaviorSubject<TestLifecycle> lifecycleSubject = BehaviorSubject.create();

    @Override
    public Observable lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    public Function correspondingEvents() {
        return new Function<TestLifecycle, TestLifecycle>() {
            @Override
            public TestLifecycle apply(@NonNull TestLifecycle testLifecycle) {
                switch (testLifecycle) {
                    case STARTED:
                        return TestLifecycle.STOPPED;
                    case STOPPED:
                        throw new LifecycleEndedException();
                    default:
                        throw new IllegalStateException("Unknown lifecycle event.");
                }
            }
        };
    }

    @Override
    public Object peekLifecycle() {
        return lifecycleSubject.getValue();
    }

    /**
     * Start the test lifecycle.
     */
    public void start() {
        lifecycleSubject.onNext(TestLifecycle.STARTED);
    }

    /**
     * Stop the test lifecycle.
     */
    public void stop() {
        lifecycleSubject.onNext(TestLifecycle.STOPPED);
    }

    private enum TestLifecycle {
        STARTED,
        STOPPED
    }
}
