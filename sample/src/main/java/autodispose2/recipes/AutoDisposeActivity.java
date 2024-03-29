/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.recipes;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import autodispose2.lifecycle.CorrespondingEventsFunction;
import autodispose2.lifecycle.LifecycleEndedException;
import autodispose2.lifecycle.LifecycleScopeProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * An {@link Activity} example implementation for making one implement {@link
 * LifecycleScopeProvider}. One would normally use this as a base activity class to extend others
 * from.
 */
public abstract class AutoDisposeActivity extends Activity
    implements LifecycleScopeProvider<AutoDisposeActivity.ActivityEvent> {

  public enum ActivityEvent {
    CREATE,
    START,
    RESUME,
    PAUSE,
    STOP,
    DESTROY
  }

  /**
   * This is a function of current event -> target disposal event. That is to say that if event A
   * returns B, then any stream subscribed to during A will autodispose on B. In Android, we make
   * symmetric boundary conditions. Create -> Destroy, Start -> Stop, etc. For anything after Resume
   * we dispose on the next immediate destruction event. Subscribing after Destroy is an error.
   */
  private static final CorrespondingEventsFunction<ActivityEvent> CORRESPONDING_EVENTS =
      activityEvent -> {
        switch (activityEvent) {
          case CREATE:
            return ActivityEvent.DESTROY;
          case START:
            return ActivityEvent.STOP;
          case RESUME:
            return ActivityEvent.PAUSE;
          case PAUSE:
            return ActivityEvent.STOP;
          case STOP:
            return ActivityEvent.DESTROY;
          default:
            throw new LifecycleEndedException("Cannot bind to Activity lifecycle after destroy.");
        }
      };

  private final BehaviorSubject<ActivityEvent> lifecycleEvents = BehaviorSubject.create();

  @Override
  public Observable<ActivityEvent> lifecycle() {
    return lifecycleEvents.hide();
  }

  @Override
  public CorrespondingEventsFunction<ActivityEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Nullable
  @Override
  public ActivityEvent peekLifecycle() {
    return lifecycleEvents.getValue();
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    lifecycleEvents.onNext(ActivityEvent.CREATE);
  }

  @Override
  protected void onStart() {
    super.onStart();
    lifecycleEvents.onNext(ActivityEvent.START);
  }

  @Override
  protected void onResume() {
    super.onResume();
    lifecycleEvents.onNext(ActivityEvent.RESUME);
  }

  @Override
  protected void onPause() {
    lifecycleEvents.onNext(ActivityEvent.PAUSE);
    super.onPause();
  }

  @Override
  protected void onStop() {
    lifecycleEvents.onNext(ActivityEvent.STOP);
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    lifecycleEvents.onNext(ActivityEvent.DESTROY);
    super.onDestroy();
  }
}
