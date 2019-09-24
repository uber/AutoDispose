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
package autodispose2.android.lifecycle.test;

import static androidx.annotation.RestrictTo.Scope.TESTS;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

/**
 * A test {@link LifecycleOwner} implementation for testing. You can either back it with your own
 * instance or just stub it in place and use its public emit() API.
 */
@RestrictTo(TESTS)
public final class TestLifecycleOwner implements LifecycleOwner {

  private final LifecycleRegistry registry;

  /** Default creator. Creates and maintains its own {@link LifecycleRegistry} under the hood. */
  public static TestLifecycleOwner create() {
    return new TestLifecycleOwner(null);
  }

  /**
   * @param registry an optional custom {@link LifecycleRegistry} if you want to provide one. If
   *     {@code null}, a default implementation will be created and maintained under the hood.
   */
  public static TestLifecycleOwner create(LifecycleRegistry registry) {
    return new TestLifecycleOwner(registry);
  }

  private TestLifecycleOwner(@Nullable LifecycleRegistry registry) {
    this.registry = registry == null ? new LifecycleRegistry(this) : registry;
  }

  @Override
  public LifecycleRegistry getLifecycle() {
    return registry;
  }

  /**
   * Simulates the emission of a given lifecycle {@code event}, marking state as necessary to the
   * internal {@link LifecycleRegistry} as well as needed.
   *
   * @param event the event to simulate
   */
  public void emit(Lifecycle.Event event) {
    registry.handleLifecycleEvent(event);
  }
}
