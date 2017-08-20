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

package com.uber.autodispose.android.archcomponents;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.TESTS;

/**
 * LifecycleOwnerScopeProvider implementation for testing. You can either back it with your own
 * instance, or just stub it in place and use its public emit() API.
 */
@RestrictTo(TESTS) public final class TestLifecycleOwnerScopeProvider
    implements LifecycleRegistryOwner {

  private final LifecycleRegistry registry;

  public TestLifecycleOwnerScopeProvider() {
    this(new LifecycleRegistry(null));
  }

  public TestLifecycleOwnerScopeProvider(@Nullable LifecycleRegistry registry) {
    this.registry = registry == null ? new LifecycleRegistry(this) : registry;
  }

  @Override public LifecycleRegistry getLifecycle() {
    return registry;
  }

  public void emit(Lifecycle.Event event) {
    registry.handleLifecycleEvent(event);
  }
}
