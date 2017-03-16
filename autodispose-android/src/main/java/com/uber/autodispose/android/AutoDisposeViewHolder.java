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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.uber.autodispose.ScopeProvider;
import io.reactivex.Maybe;
import io.reactivex.subjects.MaybeSubject;

/**
 * A ViewHolder implementation that facilitates AutoDispose scope binding for RxJava subscriptions
 * in RecyclerViews. You can extend from this base class, or copy its logic into your own ViewHolder
 * implementations and use this as a reference. When hooked up properly, you can safely scope
 * subscriptions with AutoDispose to the ViewHolder. Just make sure to implement and call
 * {@link #onViewRecycled} from your corresponding {@link RecyclerView.Adapter#onViewRecycled}
 * method to ensure proper scoping.
 */
public abstract class AutoDisposeViewHolder extends RecyclerView.ViewHolder
    implements ScopeProvider {

  private MaybeSubject<Object> unbindNotifier;

  public AutoDisposeViewHolder(View itemView) {
    super(itemView);
  }

  private void onUnBind() {
    emitUnBindIfPresent();
  }

  private MaybeSubject<?> getOrInitNotifier() {
    if (unbindNotifier == null) {
      unbindNotifier = MaybeSubject.create();
    }
    return unbindNotifier;
  }

  private void emitUnBindIfPresent() {
    if (unbindNotifier != null && !unbindNotifier.hasComplete()) {
      unbindNotifier.onSuccess(new Object());
    }
  }

  @Override public Maybe<?> requestScope() {
    return getOrInitNotifier();
  }

  /**
   * Convenience creator for a RecyclerListener that can be registered via
   * {@link RecyclerView#setRecyclerListener}. The returned listener will automatically signal
   * recycle events to {@link AutoDisposeViewHolder} holders recycled by the RecyclerView.
   *
   * @return the listener.
   */
  public static RecyclerView.RecyclerListener newRecyclerListener() {
    return new RecyclerView.RecyclerListener() {
      @Override public void onViewRecycled(RecyclerView.ViewHolder holder) {
        AutoDisposeViewHolder.onViewRecycled(holder);
      }
    };
  }

  /**
   * Proxy for {@link RecyclerView.Adapter#onViewRecycled} method to unbind a holder if it's an
   * RxViewHolder instance.
   *
   * @param holder the holder to check.
   */
  public static void onViewRecycled(RecyclerView.ViewHolder holder) {
    if (holder instanceof AutoDisposeViewHolder) {
      ((AutoDisposeViewHolder) holder).onUnBind();
    }
  }
}
