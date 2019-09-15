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

import io.reactivex.Completable;
import io.reactivex.functions.Consumer;

/** Utilities for dealing with AutoDispose scopes. */
public final class Scopes {

  private Scopes() {}

  /**
   * @return a {@link Completable} representation of the given {@code scopeProvider}. This will be
   *     deferred appropriately and handle {@link OutsideScopeException OutsideScopeExceptions}.
   */
  public static Completable completableOf(ScopeProvider scopeProvider) {
    return Completable.defer(
        () -> {
          try {
            return scopeProvider.requestScope();
          } catch (OutsideScopeException e) {
            Consumer<? super OutsideScopeException> handler =
                AutoDisposePlugins.getOutsideScopeHandler();
            if (handler != null) {
              handler.accept(e);
              return Completable.complete();
            } else {
              return Completable.error(e);
            }
          }
        });
  }
}
