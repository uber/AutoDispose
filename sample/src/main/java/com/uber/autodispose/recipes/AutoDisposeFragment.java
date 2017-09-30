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

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.LifecycleScopeProvider;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

/**
 * A {@link Fragment} example implementation for making one implement {@link
 * LifecycleScopeProvider}. One would normally use this as a base fragment class to extend others
 * from.
 */
public abstract class AutoDisposeFragment extends Fragment
    implements LifecycleScopeProvider<AutoDisposeFragment.FragmentEvent> {

  public enum FragmentEvent {
    ATTACH, CREATE, CREATE_VIEW, START, RESUME, PAUSE, STOP, DESTROY_VIEW, DESTROY, DETACH
  }

  private static Function<FragmentEvent, FragmentEvent> CORRESPONDING_EVENTS =
      new Function<FragmentEvent, FragmentEvent>() {
        @Override public FragmentEvent apply(FragmentEvent event) throws Exception {
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
        }
      };

  private final BehaviorSubject<FragmentEvent> lifecycleEvents = BehaviorSubject.create();

  @Override public Observable<FragmentEvent> lifecycle() {
    return lifecycleEvents.hide();
  }

  @Override public Function<FragmentEvent, FragmentEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Nullable @Override public FragmentEvent peekLifecycle() {
    return lifecycleEvents.getValue();
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    lifecycleEvents.onNext(FragmentEvent.ATTACH);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    lifecycleEvents.onNext(FragmentEvent.CREATE);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    lifecycleEvents.onNext(FragmentEvent.CREATE_VIEW);
  }

  @Override public void onStart() {
    super.onStart();
    lifecycleEvents.onNext(FragmentEvent.START);
  }

  @Override public void onResume() {
    super.onResume();
    lifecycleEvents.onNext(FragmentEvent.RESUME);
  }

  @Override public void onPause() {
    lifecycleEvents.onNext(FragmentEvent.PAUSE);
    super.onPause();
  }

  @Override public void onStop() {
    lifecycleEvents.onNext(FragmentEvent.STOP);
    super.onStop();
  }

  @Override public void onDestroyView() {
    lifecycleEvents.onNext(FragmentEvent.DESTROY_VIEW);
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    lifecycleEvents.onNext(FragmentEvent.DESTROY);
    super.onDestroy();
  }

  @Override public void onDetach() {
    lifecycleEvents.onNext(FragmentEvent.DETACH);
    super.onDetach();
  }
}
