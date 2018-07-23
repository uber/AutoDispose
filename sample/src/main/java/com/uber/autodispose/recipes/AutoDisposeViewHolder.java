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

import android.support.v7.widget.BindAwareViewHolder;
import android.view.View;

import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.lifecycle.LifecycleScopeProvider;

import org.jetbrains.annotations.Nullable;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Example implementation of a {@link android.support.v7.widget.RecyclerView.ViewHolder}
 * implementation that implements {@link LifecycleScopeProvider}. This could be useful for cases
 * where you have subscriptions that should be disposed upon unbinding or otherwise aren't
 * overwritten in future binds.
 */
public abstract class AutoDisposeViewHolder
    extends BindAwareViewHolder
    implements LifecycleScopeProvider<AutoDisposeViewHolder.ViewHolderEvent> {

  public enum ViewHolderEvent {
    BIND, UNBIND
  }

  private static final Function<ViewHolderEvent, ViewHolderEvent> CORRESPONDING_EVENTS =
      new Function<ViewHolderEvent, ViewHolderEvent>() {
        @Override
        public ViewHolderEvent apply(final ViewHolderEvent viewHolderEvent) throws Exception {
          switch (viewHolderEvent) {
            case BIND:
              return ViewHolderEvent.UNBIND;
            default:
              throw new LifecycleEndedException("Cannot use ViewHolder lifecycle after unbind.");
          }
        }
      };

  private final BehaviorSubject<ViewHolderEvent> lifecycleEvents = BehaviorSubject.create();

  public AutoDisposeViewHolder(View itemView) {
    super(itemView);
  }

  @Override public Function<ViewHolderEvent, ViewHolderEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Override public Observable<ViewHolderEvent> lifecycle() {
    return lifecycleEvents.hide();
  }

  @Nullable @Override public ViewHolderEvent peekLifecycle() {
    return lifecycleEvents.getValue();
  }

  @Override protected void onBind() {
    lifecycleEvents.onNext(ViewHolderEvent.BIND);
  }

  @Override protected void onUnbind() {
    lifecycleEvents.onNext(ViewHolderEvent.UNBIND);
  }
}
