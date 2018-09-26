/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uber.autodispose.android.internal;

import android.support.annotation.RestrictTo;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Copy of the MainThreadDisposable from RxAndroid which makes use of
 * {@link AutoDisposeAndroidUtil#isMainThread()}. This allows
 * disposing on the JVM without crashing due to the looper check (which is
 * often stubbed in tests).
 */
@RestrictTo(LIBRARY_GROUP)
public abstract class MainThreadDisposable implements Disposable {
  private final AtomicBoolean unsubscribed = new AtomicBoolean();

  @Override
  public final boolean isDisposed() {
    return unsubscribed.get();
  }

  @Override
  public final void dispose() {
    if (unsubscribed.compareAndSet(false, true)) {
      if (AutoDisposeAndroidUtil.isMainThread()) {
        onDispose();
      } else {
        AndroidSchedulers.mainThread().scheduleDirect(this::onDispose);
      }
    }
  }

  protected abstract void onDispose();
}
