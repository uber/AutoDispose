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
package autodispose2.android.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Looper;
import androidx.annotation.RestrictTo;
import autodispose2.android.AutoDisposeAndroidPlugins;
import io.reactivex.rxjava3.functions.BooleanSupplier;

@RestrictTo(LIBRARY_GROUP)
public class AutoDisposeAndroidUtil {

  private static final BooleanSupplier MAIN_THREAD_CHECK =
      () -> Looper.myLooper() == Looper.getMainLooper();

  private AutoDisposeAndroidUtil() {}

  public static boolean isMainThread() {
    return AutoDisposeAndroidPlugins.onCheckMainThread(MAIN_THREAD_CHECK);
  }
}
