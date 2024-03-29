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

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import autodispose2.android.ViewScopeProvider;
import autodispose2.lifecycle.CorrespondingEventsFunction;
import autodispose2.lifecycle.LifecycleEndedException;
import autodispose2.lifecycle.LifecycleScopeProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * An example implementation of an AutoDispose View with lifecycle handling and precondition checks
 * using {@link LifecycleScopeProvider}. The precondition checks here are only different from what
 * {@link ViewScopeProvider} provides in that it will check against subscription in the constructor.
 */
public abstract class AutoDisposeView extends View
    implements LifecycleScopeProvider<AutoDisposeView.ViewEvent> {

  /**
   * This is a function of current event -> target disposal event. That is to say that if event
   * "Attach" returns "Detach", then any stream subscribed to during Attach will autodispose on
   * Detach.
   */
  private static final CorrespondingEventsFunction<ViewEvent> CORRESPONDING_EVENTS =
      viewEvent -> {
        switch (viewEvent) {
          case ATTACH:
            return ViewEvent.DETACH;
          default:
            throw new LifecycleEndedException("Cannot bind to View lifecycle after detach.");
        }
      };

  @Nullable private BehaviorSubject<ViewEvent> lifecycleEvents = null;

  public AutoDisposeView(Context context) {
    this(context, null);
  }

  public AutoDisposeView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, View.NO_ID);
  }

  public AutoDisposeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public AutoDisposeView(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    if (!isInEditMode()) {
      // This is important to gate so you don't break the IDE preview!
      lifecycleEvents = BehaviorSubject.create();
    }
  }

  public enum ViewEvent {
    ATTACH,
    DETACH
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (lifecycleEvents != null) {
      lifecycleEvents.onNext(ViewEvent.ATTACH);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (lifecycleEvents != null) {
      lifecycleEvents.onNext(ViewEvent.DETACH);
    }
  }

  @SuppressWarnings("NullAway") // only null in layoutlib
  @Override
  public Observable<ViewEvent> lifecycle() {
    //noinspection ConstantConditions only in layoutlib
    return lifecycleEvents.hide();
  }

  @Override
  public CorrespondingEventsFunction<ViewEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @SuppressWarnings("NullAway") // only null in layoutlib
  @Nullable
  @Override
  public ViewEvent peekLifecycle() {
    //noinspection ConstantConditions only in layoutlib
    return lifecycleEvents.getValue();
  }
}
