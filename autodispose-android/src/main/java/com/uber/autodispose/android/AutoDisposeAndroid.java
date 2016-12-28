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
    return AutoDispose.observable().withScope(new ViewLifecycleScopeProvider(view));
  }

  public static SingleSubscribeClause single(View view) {
    return AutoDispose.single().withScope(new ViewLifecycleScopeProvider(view));
  }

  public static MaybeSubscribeClause maybe(View view) {
    return AutoDispose.maybe().withScope(new ViewLifecycleScopeProvider(view));
  }

  public static CompletableSubscribeClause completable(View view) {
    return AutoDispose.completable().withScope(new ViewLifecycleScopeProvider(view));
  }

  public static FlowableSubscribeClause flowable(View view) {
    return AutoDispose.flowable().withScope(new ViewLifecycleScopeProvider(view));
  }
}
