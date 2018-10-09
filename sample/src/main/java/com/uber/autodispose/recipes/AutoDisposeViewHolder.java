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

import androidx.appcompat.widget.BindAwareViewHolder;
import android.view.View;
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction;
import com.uber.autodispose.lifecycle.LifecycleEndedException;
import com.uber.autodispose.lifecycle.LifecycleScopeProvider;
import com.uber.autodispose.lifecycle.LifecycleScopes;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.annotations.Nullable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Example implementation of a {@link androidx.appcompat.widget.RecyclerView.ViewHolder}
 * implementation that implements {@link LifecycleScopeProvider}. This could be useful for cases
 * where you have subscriptions that should be disposed upon unbinding or otherwise aren't
 * overwritten in future binds.
 */
public abstract class AutoDisposeViewHolder extends BindAwareViewHolder
    implements LifecycleScopeProvider<AutoDisposeViewHolder.ViewHolderEvent> {

  public enum ViewHolderEvent {
    BIND, UNBIND
  }

  private static final CorrespondingEventsFunction<ViewHolderEvent> CORRESPONDING_EVENTS = viewHolderEvent -> {
    switch (viewHolderEvent) {
      case BIND:
        return ViewHolderEvent.UNBIND;
      default:
        throw new LifecycleEndedException("Cannot use ViewHolder lifecycle after unbind.");
    }
  };

  private final BehaviorSubject<ViewHolderEvent> lifecycleEvents = BehaviorSubject.create();

  public AutoDisposeViewHolder(View itemView) {
    super(itemView);
  }

  @Override public CorrespondingEventsFunction<ViewHolderEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Override public Observable<ViewHolderEvent> lifecycle() {
    return lifecycleEvents.hide();
  }

  @Nullable @Override public ViewHolderEvent peekLifecycle() {
    return lifecycleEvents.getValue();
  }

  @Override public CompletableSource requestScope() {
    return LifecycleScopes.resolveScopeFromLifecycle(this);
  }

  @Override protected void onBind() {
    lifecycleEvents.onNext(ViewHolderEvent.BIND);
  }

  @Override protected void onUnbind() {
    lifecycleEvents.onNext(ViewHolderEvent.UNBIND);
  }
}
