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

import android.view.View;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.clause.subscribe.CompletableSubscribeClause;
import com.uber.autodispose.clause.subscribe.FlowableSubscribeClause;
import com.uber.autodispose.clause.subscribe.MaybeSubscribeClause;
import com.uber.autodispose.clause.subscribe.ObservableSubscribeClause;
import com.uber.autodispose.clause.subscribe.SingleSubscribeClause;

public final class AutoDisposeAndroid {

  private AutoDisposeAndroid() {
    throw new InstantiationError();
  }

  public static ObservableSubscribeClause observable(View view) {
    return AutoDispose.observable()
        .withScope(new ViewLifecycleScopeProvider(view));
  }

  public static SingleSubscribeClause single(View view) {
    return AutoDispose.single()
        .withScope(new ViewLifecycleScopeProvider(view));
  }

  public static MaybeSubscribeClause maybe(View view) {
    return AutoDispose.maybe()
        .withScope(new ViewLifecycleScopeProvider(view));
  }

  public static CompletableSubscribeClause completable(View view) {
    return AutoDispose.completable()
        .withScope(new ViewLifecycleScopeProvider(view));
  }

  public static FlowableSubscribeClause flowable(View view) {
    return AutoDispose.flowable()
        .withScope(new ViewLifecycleScopeProvider(view));
  }
}
