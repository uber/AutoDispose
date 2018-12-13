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

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.subjects.CompletableSubject;

/**
 * ScopeProvider implementation for testing. You can either back it with your own instance, or just
 * stub it in place and use its public emit APIs.
 */
public final class TestScopeProvider implements ScopeProvider {

  /**
   * Creates a new provider backed by an internal {@link CompletableSubject}. Useful for stubbing or if you only
   * want to use the emit APIs
   *
   * @return the created TestScopeProvider.
   */
  public static TestScopeProvider create() {
    return create(CompletableSubject.create());
  }

  /**
   * Creates a new provider backed by {@code delegate}.
   *
   * @param delegate the delegate to back this with.
   * @return the created TestScopeProvider.
   */
  public static TestScopeProvider create(Completable delegate) {
    return new TestScopeProvider(delegate);
  }

  private final CompletableSubject innerScope = CompletableSubject.create();

  @SuppressWarnings("AutoDispose")
  private TestScopeProvider(Completable delegate) {
    delegate.subscribe(innerScope);
  }

  @Override public CompletableSource requestScope() {
    return innerScope;
  }

  /**
   * Emits a success event, just a simple Object.
   */
  public void emit() {
    innerScope.onComplete();
  }
}
