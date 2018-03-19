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

package com.uber.autodispose.android.internal;

import android.os.Build;
import android.os.Looper;
import android.support.annotation.RestrictTo;
import android.view.View;
import com.uber.autodispose.android.AutoDisposeAndroidPlugins;
import io.reactivex.functions.BooleanSupplier;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

@RestrictTo(LIBRARY_GROUP)
public class AutoDisposeAndroidUtil {

  private static final BooleanSupplier MAIN_THREAD_CHECK = new BooleanSupplier() {
    @Override public boolean getAsBoolean() {
      return Looper.myLooper() == Looper.getMainLooper();
    }
  };

  private AutoDisposeAndroidUtil() { }

  public static boolean isMainThread() {
    return AutoDisposeAndroidPlugins.onCheckMainThread(MAIN_THREAD_CHECK);
  }

  public static boolean isAttached(View view) {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && view.isAttachedToWindow())
        || view.getWindowToken() != null;
  }
}
