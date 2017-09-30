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

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.LifecycleScopeProvider;
import com.uber.autodispose.android.ViewScopeProvider;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

/**
 * An example implementation of an AutoDispose View with lifecycle handling and precondition checks
 * using {@link LifecycleScopeProvider}. The precondition checks here are only different from what
 * {@link ViewScopeProvider} provides in that it will check against subscription in the constructor.
 */
public abstract class AutoDisposeView extends View
    implements LifecycleScopeProvider<AutoDisposeView.ViewEvent> {

  private static Function<ViewEvent, ViewEvent> CORRESPONDING_EVENTS =
      new Function<ViewEvent, ViewEvent>() {
        @Override public ViewEvent apply(ViewEvent viewEvent) throws Exception {
          switch (viewEvent) {
            case ATTACH:
              return ViewEvent.DETACH;
            default:
              throw new LifecycleEndedException("Cannot bind to View lifecycle after detach.");
          }
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
  public AutoDisposeView(Context context,
      @Nullable AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes) {
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
    ATTACH, DETACH
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (lifecycleEvents != null) {
      lifecycleEvents.onNext(ViewEvent.ATTACH);
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (lifecycleEvents != null) {
      lifecycleEvents.onNext(ViewEvent.DETACH);
    }
  }

  @Override public Observable<ViewEvent> lifecycle() {
    return lifecycleEvents.hide();
  }

  @Override public Function<ViewEvent, ViewEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Nullable @Override public ViewEvent peekLifecycle() {
    return lifecycleEvents.getValue();
  }
}
