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

package com.uber.autodispose.recipes;

import android.app.Activity;
import android.os.Bundle;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.LifecycleScopeProvider;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import javax.annotation.Nullable;

/**
 * An {@link Activity} example implementation for making one implement {@link
 * LifecycleScopeProvider}.
 */
public abstract class AutoDisposeActivity extends Activity
    implements LifecycleScopeProvider<AutoDisposeActivity.ActivityEvent> {

  public enum ActivityEvent {
    CREATE, START, RESUME, PAUSE, STOP, DESTROY
  }

  private static Function<ActivityEvent, ActivityEvent> CORRESPONDING_EVENTS =
      new Function<ActivityEvent, ActivityEvent>() {
        @Override public ActivityEvent apply(ActivityEvent activityEvent) throws Exception {
          switch (activityEvent) {
            case CREATE:
              return ActivityEvent.DESTROY;
            case START:
              return ActivityEvent.STOP;
            case RESUME:
            case PAUSE:
            case STOP:
              return ActivityEvent.DESTROY;
            default:
              throw new LifecycleEndedException();
          }
        }
      };

  private final BehaviorSubject<ActivityEvent> lifecycleEvents = BehaviorSubject.create();

  @Override public Observable<ActivityEvent> lifecycle() {
    return lifecycleEvents.hide();
  }

  @Override public Function<ActivityEvent, ActivityEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Nullable @Override public ActivityEvent peekLifecycle() {
    return lifecycleEvents.getValue();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    lifecycleEvents.onNext(ActivityEvent.CREATE);
  }

  @Override protected void onStart() {
    super.onStart();
    lifecycleEvents.onNext(ActivityEvent.START);
  }

  @Override protected void onResume() {
    super.onResume();
    lifecycleEvents.onNext(ActivityEvent.RESUME);
  }

  @Override protected void onPause() {
    lifecycleEvents.onNext(ActivityEvent.PAUSE);
    super.onPause();
  }

  @Override protected void onStop() {
    lifecycleEvents.onNext(ActivityEvent.STOP);
    super.onStop();
  }

  @Override protected void onDestroy() {
    lifecycleEvents.onNext(ActivityEvent.DESTROY);
    super.onDestroy();
  }
}
