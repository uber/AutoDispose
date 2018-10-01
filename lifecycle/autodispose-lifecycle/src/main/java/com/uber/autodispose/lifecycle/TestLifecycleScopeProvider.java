/*
 * Copyright (C) 2018. Uber Technologies
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

package com.uber.autodispose.lifecycle;

import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.annotations.Nullable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Test utility to create {@link LifecycleScopeProvider} instances for tests.
 *
 * <p>Supports a start and stop lifecycle. Subscribing when outside of the lifecycle will throw
 * either a {@link LifecycleNotStartedException} or {@link LifecycleEndedException}.
 */
public final class TestLifecycleScopeProvider
    implements LifecycleScopeProvider<TestLifecycleScopeProvider.TestLifecycle> {

  private final BehaviorSubject<TestLifecycle> lifecycleSubject;

  private TestLifecycleScopeProvider(@Nullable TestLifecycle initialValue) {
    if (initialValue == null) {
      lifecycleSubject = BehaviorSubject.create();
    } else {
      lifecycleSubject = BehaviorSubject.createDefault(initialValue);
    }
  }

  /**
   * @return a new {@link TestLifecycleScopeProvider} instance.
   */
  public static TestLifecycleScopeProvider create() {
    return new TestLifecycleScopeProvider(null);
  }

  /**
   * @param initialValue the initial lifecycle event to create the {@link
   * TestLifecycleScopeProvider} with.
   * @return a new {@link TestLifecycleScopeProvider} instance with {@param initialValue} as its
   * initial lifecycle
   * event.
   */
  public static TestLifecycleScopeProvider createInitial(TestLifecycle initialValue) {
    return new TestLifecycleScopeProvider(initialValue);
  }

  @Override public Observable<TestLifecycle> lifecycle() {
    return lifecycleSubject.hide();
  }

  @Override public CorrespondingEventsFunction<TestLifecycle> correspondingEvents() {
    return testLifecycle -> {
      switch (testLifecycle) {
        case STARTED:
          return TestLifecycle.STOPPED;
        case STOPPED:
          throw new LifecycleEndedException();
        default:
          throw new IllegalStateException("Unknown lifecycle event.");
      }
    };
  }

  @Override public TestLifecycle peekLifecycle() {
    return lifecycleSubject.getValue();
  }

  @Override public CompletableSource requestScope() {
    return LifecycleScopes.resolveScopeFromLifecycle(this);
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
    if (lifecycleSubject.getValue() != TestLifecycle.STARTED) {
      throw new IllegalStateException("Attempting to stop lifecycle before starting it.");
    }
    lifecycleSubject.onNext(TestLifecycle.STOPPED);
  }

  public enum TestLifecycle {
    STARTED, STOPPED
  }
}
