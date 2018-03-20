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

package com.uber.autodispose.android;

import android.support.annotation.Nullable;
import android.view.View;
import com.uber.autodispose.LifecycleScopeProvider;
import com.uber.autodispose.OutsideLifecycleException;
import com.uber.autodispose.android.internal.AutoDisposeAndroidUtil;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import static com.uber.autodispose.android.ViewLifecycleEvent.DETACH;

/**
 * A {@link LifecycleScopeProvider} that can provide scoping for Android {@link View} classes.
 * <p>
 * <pre><code>
 *   AutoDispose.autoDisposable(ViewScopeProvider.from(view));
 * </code></pre>
 */
public class ViewScopeProvider implements LifecycleScopeProvider<ViewLifecycleEvent> {
  private static final Function<ViewLifecycleEvent, ViewLifecycleEvent> CORRESPONDING_EVENTS =
      new Function<ViewLifecycleEvent, ViewLifecycleEvent>() {
        @Override public ViewLifecycleEvent apply(ViewLifecycleEvent lastEvent) throws Exception {
          switch (lastEvent) {
            case ATTACH:
              return DETACH;
            default:
              throw new OutsideLifecycleException("View is detached!");
          }
        }
      };

  private final Observable<ViewLifecycleEvent> lifecycle;
  private final View view;

  /**
   * Creates a {@link LifecycleScopeProvider} for Android Views.
   *
   * @param view the view to scope for
   * @return a {@link LifecycleScopeProvider} against this view.
   */
  public static LifecycleScopeProvider<ViewLifecycleEvent> from(@Nullable View view) {
    if (view == null) {
      throw new NullPointerException("view == null");
    }
    return new ViewScopeProvider(view);
  }

  private ViewScopeProvider(final View view) {
    this.view = view;
    lifecycle = new ViewAttachEventsObservable(view);
  }

  @Override public Observable<ViewLifecycleEvent> lifecycle() {
    return lifecycle;
  }

  @Override public Function<ViewLifecycleEvent, ViewLifecycleEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Override public ViewLifecycleEvent peekLifecycle() {
    return AutoDisposeAndroidUtil.isAttached(view) ? ViewLifecycleEvent.ATTACH : DETACH;
  }
}
