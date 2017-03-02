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

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.MaybeSubject;

final class TestUtil {
  private static final Function<Integer, Integer> CORRESPONDING_EVENTS =
      new Function<Integer, Integer>() {
        @Override public Integer apply(Integer lastEvent) throws Exception {
          switch (lastEvent) {
            case 0:
              return 3;
            case 1:
              return 2;
            default:
              throw new LifecycleEndedException();
          }
        }
      };

  private TestUtil() {
    throw new InstantiationError();
  }

  static ScopeProvider makeProvider(final MaybeSubject<Integer> scope) {
    return new ScopeProvider() {
      @Override public Maybe<?> requestScope() {
        return scope;
      }
    };
  }

  static LifecycleScopeProvider<Integer> makeLifecycleProvider(
      final BehaviorSubject<Integer> lifecycle) {
    return new LifecycleScopeProvider<Integer>() {
      @NonNull @Override public Observable<Integer> lifecycle() {
        return lifecycle;
      }

      @NonNull @Override public Function<Integer, Integer> correspondingEvents() {
        return CORRESPONDING_EVENTS;
      }

      @Override public Integer peekLifecycle() {
        return lifecycle.getValue();
      }
    };
  }
}
