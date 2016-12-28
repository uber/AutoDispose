/*
 * Copyright (c) 2016. Uber Technologies
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

import android.os.Build;
import android.view.View;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.LifecycleScopeProvider;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;

import static com.uber.autodispose.android.Util.isMainThread;

class ViewLifecycleScopeProvider implements LifecycleScopeProvider<ViewLifecycleEvent> {
  private static final Function<ViewLifecycleEvent, ViewLifecycleEvent> CORRESPONDING_EVENTS =
      new Function<ViewLifecycleEvent, ViewLifecycleEvent>() {
        @Override public ViewLifecycleEvent apply(ViewLifecycleEvent lastEvent) throws Exception {
          switch (lastEvent) {
            case ATTACH:
              return ViewLifecycleEvent.DETACH;
            default:
              throw new LifecycleEndedException();
          }
        }
      };

  private final Observable<ViewLifecycleEvent> lifecycle;
  private final View view;

  ViewLifecycleScopeProvider(final View view) {
    this.view = view;
    lifecycle = Observable.create(new ObservableOnSubscribe<ViewLifecycleEvent>() {
      @Override public void subscribe(final ObservableEmitter<ViewLifecycleEvent> e)
          throws Exception {
        if (!isMainThread()) {
          throw new IllegalStateException("Views can only be bound to on the main thread!");
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && view.isAttachedToWindow())
            || view.getWindowToken() != null) {
          // Emit the last event, like a behavior subject
          e.onNext(ViewLifecycleEvent.ATTACH);
        }

        final View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
          @Override public void onViewAttachedToWindow(View view1) {
            e.onNext(ViewLifecycleEvent.ATTACH);
          }

          @Override public void onViewDetachedFromWindow(View view1) {
            e.onNext(ViewLifecycleEvent.DETACH);
          }
        };

        e.setCancellable(new Cancellable() {
          @Override public void cancel() throws Exception {
            // A "main thread cancellable"
            if (isMainThread()) {
              onCancel();
            } else {
              AndroidSchedulers.mainThread()
                  .createWorker()
                  .schedule(new Runnable() {
                    @Override public void run() {
                      onCancel();
                    }
                  });
            }
          }

          private void onCancel() {
            view.removeOnAttachStateChangeListener(listener);
          }
        });
      }
    });
  }

  @Override public Observable<ViewLifecycleEvent> lifecycle() {
    return lifecycle;
  }

  @Override public Function<ViewLifecycleEvent, ViewLifecycleEvent> correspondingEvents() {
    return CORRESPONDING_EVENTS;
  }

  @Override public ViewLifecycleEvent peekLifecycle() {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && view.isAttachedToWindow())
        || view.getWindowToken() != null ? ViewLifecycleEvent.ATTACH : ViewLifecycleEvent.DETACH;
  }
}
