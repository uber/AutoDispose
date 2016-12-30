package com.uber.autodispose.android;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.FrameLayout;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.OutsideLifecycleException;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class) public final class ViewLifecycleScopeProviderTest {

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
    final RecordingObserver<Integer> o = new RecordingObserver<>();
    final PublishSubject<Integer> subject = PublishSubject.create();

    // Attach it
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        parent.addView(child);
      }
    });
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        subject.subscribe(AutoDispose.observable()
            .withScope(ViewLifecycleScopeProvider.from(child))
            .around(o));
      }
    });

    Disposable d = o.takeSubscribe();
    o.assertNoMoreEvents(); // No initial value.

    subject.onNext(0);
    assertThat(o.takeNext()).isEqualTo(0);

    subject.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        parent.removeView(child);
      }
    });

    subject.onNext(2);
    o.assertNoMoreEvents();

    d.dispose();
  }

  @Test public void observable_offMainThread_shouldFail() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    PublishSubject<Integer> subject = PublishSubject.create();

    // Attach it
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        parent.addView(child);
      }
    });
    subject.subscribe(AutoDispose.observable()
        .withScope(ViewLifecycleScopeProvider.from(child))
        .around(o));

    Disposable d = o.takeSubscribe();
    Throwable t = o.takeError();
    assertThat(t).isInstanceOf(IllegalStateException.class);
    assertThat(t.getMessage()).contains("main thread");
    o.assertNoMoreEvents();
    assertThat(d.isDisposed()).isTrue();
  }

  @Test public void observable_offBeforeAttach_shouldFail() {
    final RecordingObserver<Integer> o = new RecordingObserver<>();
    final PublishSubject<Integer> subject = PublishSubject.create();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        subject.subscribe(AutoDispose.observable()
            .withScope(ViewLifecycleScopeProvider.from(child))
            .around(o));
      }
    });

    Disposable d = o.takeSubscribe();
    Throwable t = o.takeError();
    assertThat(t).isInstanceOf(OutsideLifecycleException.class);
    o.assertNoMoreEvents();
    assertThat(d.isDisposed()).isTrue();
  }

  @Test public void observable_offAfterDetach_shouldFail() {
    final RecordingObserver<Integer> o = new RecordingObserver<>();
    final PublishSubject<Integer> subject = PublishSubject.create();

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        parent.addView(child);
      }
    });
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        parent.removeView(child);
      }
    });
    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        subject.subscribe(AutoDispose.observable()
            .withScope(ViewLifecycleScopeProvider.from(child))
            .around(o));
      }
    });

    Disposable d = o.takeSubscribe();
    Throwable t = o.takeError();
    assertThat(t).isInstanceOf(OutsideLifecycleException.class);
    o.assertNoMoreEvents();
    assertThat(d.isDisposed()).isTrue();
  }
}
