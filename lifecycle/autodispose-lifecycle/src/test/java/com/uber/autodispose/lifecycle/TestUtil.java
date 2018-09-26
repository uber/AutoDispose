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
import io.reactivex.subjects.BehaviorSubject;

final class TestUtil {
  private static final CorrespondingEventsFunction<Integer> CORRESPONDING_EVENTS = lastEvent -> {
    switch (lastEvent) {
      case 0:
        return 3;
      case 1:
        return 2;
      default:
        throw new LifecycleEndedException();
    }
  };

  private TestUtil() {
    throw new InstantiationError();
  }

  static LifecycleScopeProvider<Integer> makeLifecycleProvider(final BehaviorSubject<Integer> lifecycle) {
    return new LifecycleScopeProvider<Integer>() {
      @Override public Observable<Integer> lifecycle() {
        return lifecycle;
      }

      @Override public CorrespondingEventsFunction<Integer> correspondingEvents() {
        return CORRESPONDING_EVENTS;
      }

      @Override public Integer peekLifecycle() {
        return lifecycle.getValue();
      }

      @Override public CompletableSource requestScope() {
        return LifecycleScopes.resolveScopeFromLifecycle(this);
      }
    };
  }
}
