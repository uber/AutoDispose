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

import android.app.Activity;
import android.support.test.espresso.IdlingResource;
import android.view.View;

/**
 * Borrowed from RxBinding - https://git.io/vyS0h.
 */
public final class ViewDirtyIdlingResource implements IdlingResource {
  private final View decorView;
  private ResourceCallback resourceCallback;

  ViewDirtyIdlingResource(Activity activity) {
    decorView = activity.getWindow()
        .getDecorView();
  }

  @Override public String getName() {
    return "view dirty";
  }

  @Override public boolean isIdleNow() {
    boolean clean = !decorView.isDirty();
    if (clean) {
      resourceCallback.onTransitionToIdle();
    }
    return clean;
  }

  @Override public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
    this.resourceCallback = resourceCallback;
  }
}
