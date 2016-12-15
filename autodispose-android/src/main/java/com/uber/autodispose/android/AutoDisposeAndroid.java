package com.uber.autodispose.android;

import android.view.View;
import com.uber.autodispose.AutoDispose;

public final class AutoDisposeAndroid {

  private AutoDisposeAndroid() {
    throw new InstantiationError();
  }

  public static AutoDispose.AutoDisposingObserverCreator observable(View view) {
    return AutoDispose.observable(new ViewLifecycleProvider(view));
  }

  public static AutoDispose.AutoDisposingSingleObserverCreator single(View view) {
    return AutoDispose.single(new ViewLifecycleProvider(view));
  }

  public static AutoDispose.AutoDisposingMaybeObserverCreator maybe(View view) {
    return AutoDispose.maybe(new ViewLifecycleProvider(view));
  }

  public static AutoDispose.AutoDisposingCompletableObserverCreator completable(View view) {
    return AutoDispose.completable(new ViewLifecycleProvider(view));
  }

  public static AutoDispose.AutoDisposingSubscriberCreator flowable(View view) {
    return AutoDispose.flowable(new ViewLifecycleProvider(view));
  }
}
