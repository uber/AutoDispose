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

import android.support.annotation.Nullable;
import android.support.v7.widget.BindAwareViewHolder;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.uber.autodispose.ScopeProvider;
import io.reactivex.Maybe;
import io.reactivex.subjects.MaybeSubject;

/**
 * Example implementation of a {@link RecyclerView.ViewHolder} implementation that implements
 * {@link ScopeProvider}. This could be useful for cases where you have subscriptions that should be
 * disposed upon unbinding or otherwise aren't overwritten in future binds.
 */
public abstract class AutoDisposeViewHolder extends BindAwareViewHolder implements ScopeProvider {

  private static Object NOTIFICATION = new Object();

  @Nullable private MaybeSubject<Object> unbindNotifier = null;

  public AutoDisposeViewHolder(View itemView) {
    super(itemView);
  }

  private synchronized MaybeSubject<Object> notifier() {
    MaybeSubject<Object> n = unbindNotifier;
    if (n == null) {
      n = MaybeSubject.create();
      unbindNotifier = n;
    }
    return n;
  }

  @Override protected void onUnbind() {
    emitUnbindIfPresent();
    unbindNotifier = null;
  }

  private void emitUnbindIfPresent() {
    MaybeSubject<Object> notifier = unbindNotifier;
    if (notifier != null && !notifier.hasComplete()) {
      notifier.onSuccess(NOTIFICATION);
    }
  }

  @Override public final Maybe<?> requestScope() {
    return notifier();
  }
}
