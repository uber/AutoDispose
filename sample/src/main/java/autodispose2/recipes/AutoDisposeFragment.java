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
package autodispose2.recipes;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import autodispose2.lifecycle.CorrespondingEventsFunction;
import autodispose2.lifecycle.LifecycleEndedException;
import autodispose2.lifecycle.LifecycleScopeProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * A {@link Fragment} example implementation for making one implement {@link
 * LifecycleScopeProvider}. One would normally use this as a base fragment class to extend others
 * from.
 */
public abstract class AutoDisposeFragment extends Fragment
    implements LifecycleScopeProvider<AutoDisposeFragment.FragmentEvent> {

  public enum FragmentEvent {
    ATTACH,
    CREATE,
    CREATE_VIEW,
    START,
    RESUME,
    PAUSE,
    STOP,
    DESTROY_VIEW,
    DESTROY,
    DETACH
  }

  /**
   * This is a function of current event -> target disposal event. That is to say that if event A
   * returns B, then any stream subscribed to during A will autodispose on B. In Android, we make
   * symmetric boundary conditions. Create -> Destroy, Start -> Stop, etc. For anything after Resume
   * we dispose on the next immediate destruction event. Subscribing after Detach is an error.
   */
  private static final CorrespondingEventsFunction<FragmentEvent> CORRESPONDING_EVENTS =
      event -> {
        switch (event) {
          case ATTACH:
            return FragmentEvent.DETACH;
          case CREATE:
            return FragmentEvent.DESTROY;
          case CREATE_VIEW:
            return FragmentEvent.DESTROY_VIEW;
          case START:
            return FragmentEvent.STOP;
          case RESUME:
            return FragmentEvent.PAUSE;
          case PAUSE:
            return FragmentEvent.STOP;
          case STOP:
            return FragmentEvent.DESTROY_VIEW;
          case DESTROY_VIEW:
            return FragmentEvent.DESTROY;
          case DESTROY:
            return FragmentEvent.DETACH;
          default:
            throw new LifecycleEndedException("Cannot bind to Fragment lifecycle after detach.");
        }
      };

  private final BehaviorSubject<FragmentEvent> lifecycleEvents = BehaviorSubject.create();

  @Override
  public Observable<FragmentEvent> lifecycle() {
    return lifecycleEvents.hide();
  }

  @Override
  public CorrespondingEventsFunction<FragmentEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Nullable
  @Override
  public FragmentEvent peekLifecycle() {
    return lifecycleEvents.getValue();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    lifecycleEvents.onNext(FragmentEvent.ATTACH);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    lifecycleEvents.onNext(FragmentEvent.CREATE);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    lifecycleEvents.onNext(FragmentEvent.CREATE_VIEW);
  }

  @Override
  public void onStart() {
    super.onStart();
    lifecycleEvents.onNext(FragmentEvent.START);
  }

  @Override
  public void onResume() {
    super.onResume();
    lifecycleEvents.onNext(FragmentEvent.RESUME);
  }

  @Override
  public void onPause() {
    lifecycleEvents.onNext(FragmentEvent.PAUSE);
    super.onPause();
  }

  @Override
  public void onStop() {
    lifecycleEvents.onNext(FragmentEvent.STOP);
    super.onStop();
  }

  @Override
  public void onDestroyView() {
    lifecycleEvents.onNext(FragmentEvent.DESTROY_VIEW);
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    lifecycleEvents.onNext(FragmentEvent.DESTROY);
    super.onDestroy();
  }

  @Override
  public void onDetach() {
    lifecycleEvents.onNext(FragmentEvent.DETACH);
    super.onDetach();
  }
}
