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
package com.uber.autodispose.android.lifecycle.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.lifecycle.Lifecycle.Event.ON_CREATE;
import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static androidx.lifecycle.Lifecycle.Event.ON_RESUME;
import static androidx.lifecycle.Lifecycle.Event.ON_START;

import androidx.annotation.RestrictTo;
import androidx.lifecycle.Lifecycle;
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction;
import com.uber.autodispose.lifecycle.LifecycleEndedException;

@RestrictTo(LIBRARY_GROUP)
public final class CorrespondingEventsUtil {
  private CorrespondingEventsUtil() {}

  public static final CorrespondingEventsFunction<Lifecycle.Event> DEFAULT_CORRESPONDING_EVENTS =
      lastEvent -> {
        switch (lastEvent) {
          case ON_CREATE:
            return Lifecycle.Event.ON_DESTROY;
          case ON_START:
            return Lifecycle.Event.ON_STOP;
          case ON_RESUME:
            return Lifecycle.Event.ON_PAUSE;
          case ON_PAUSE:
            return Lifecycle.Event.ON_STOP;
          case ON_STOP:
          case ON_DESTROY:
          default:
            throw new LifecycleEndedException("Lifecycle has ended! Last event was " + lastEvent);
        }
      };

  public static Lifecycle.Event getCorrespondingEvent(Lifecycle lifecycle) {
    Lifecycle.Event correspondingEvent;
    switch (lifecycle.getCurrentState()) {
      case INITIALIZED:
        correspondingEvent = ON_CREATE;
        break;
      case CREATED:
        correspondingEvent = ON_START;
        break;
      case STARTED:
      case RESUMED:
        correspondingEvent = ON_RESUME;
        break;
      case DESTROYED:
      default:
        correspondingEvent = ON_DESTROY;
        break;
    }
    return correspondingEvent;
  }
}
