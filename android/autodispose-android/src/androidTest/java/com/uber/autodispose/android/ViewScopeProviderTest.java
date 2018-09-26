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

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.OutsideScopeException;
import com.uber.autodispose.test.RecordingObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public final class ViewScopeProviderTest {

  private static final RecordingObserver.Logger LOGGER =
      message -> Log.d(ViewScopeProviderTest.class.getSimpleName(), message);

  @Rule public final ActivityTestRule<AutoDisposeTestActivity> activityRule =
      new ActivityTestRule<>(AutoDisposeTestActivity.class);

  private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
  private FrameLayout parent;
  private View child;

  @Before public void setUp() {
    AutoDisposeTestActivity activity = activityRule.getActivity();
    parent = activity.parent;
    child = activity.child;
  }

  @Test public void observable_normal() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    // Attach it
    instrumentation.runOnMainSync(() -> parent.addView(child));
    instrumentation.runOnMainSync(() -> subject.as(AutoDispose.<Integer>autoDisposable(ViewScopeProvider.from(child)))
        .subscribe(o));

    Disposable d = o.takeSubscribe();
    o.assertNoMoreEvents(); // No initial value.

    subject.onNext(0);
    assertThat(o.takeNext()).isEqualTo(0);

    subject.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    instrumentation.runOnMainSync(() -> parent.removeView(child));

    subject.onNext(2);
    o.assertNoMoreEvents();

    d.dispose();
  }

  @Test public void observable_offMainThread_shouldFail() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> subject = PublishSubject.create();

    // Attach it
    instrumentation.runOnMainSync(() -> parent.addView(child));
    subject.as(AutoDispose.<Integer>autoDisposable(ViewScopeProvider.from(child)))
        .subscribe(o);

    Disposable d = o.takeSubscribe();
    Throwable t = o.takeError();
    assertThat(t).isInstanceOf(IllegalStateException.class);
    assertThat(t.getMessage()).contains("main thread");
    o.assertNoMoreEvents();
    assertThat(d.isDisposed()).isTrue();
  }

  @Test public void observable_offBeforeAttach_shouldFail() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    instrumentation.runOnMainSync(() -> subject.as(AutoDispose.<Integer>autoDisposable(ViewScopeProvider.from(child)))
        .subscribe(o));

    Disposable d = o.takeSubscribe();
    Throwable t = o.takeError();
    assertThat(t).isInstanceOf(OutsideScopeException.class);
    o.assertNoMoreEvents();
    assertThat(d.isDisposed()).isTrue();
  }

  @Test public void observable_offAfterDetach_shouldFail() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    instrumentation.runOnMainSync(() -> parent.addView(child));
    instrumentation.runOnMainSync(() -> parent.removeView(child));
    instrumentation.runOnMainSync(() -> subject.as(AutoDispose.<Integer>autoDisposable(ViewScopeProvider.from(child)))
        .subscribe(o));

    Disposable d = o.takeSubscribe();
    Throwable t = o.takeError();
    assertThat(t).isInstanceOf(OutsideScopeException.class);
    o.assertNoMoreEvents();
    assertThat(d.isDisposed()).isTrue();
  }
}
