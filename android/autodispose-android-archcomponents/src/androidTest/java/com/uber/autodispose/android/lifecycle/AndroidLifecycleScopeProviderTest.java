/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.autodispose.android.lifecycle;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.AutoDispose.autoDisposable;

import android.util.Log;
import androidx.lifecycle.Lifecycle;
import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.uber.autodispose.android.lifecycle.test.TestLifecycleOwner;
import com.uber.autodispose.lifecycle.LifecycleEndedException;
import com.uber.autodispose.test.RecordingObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class AndroidLifecycleScopeProviderTest {

  private static final RecordingObserver.Logger LOGGER =
      message -> Log.d(AndroidLifecycleScopeProviderTest.class.getSimpleName(), message);

  @Test
  @UiThreadTest
  public void observable_beforeCreate() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    // Spin it up
    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    subject.as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle))).subscribe(o);
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);

    Disposable d = o.takeSubscribe();
    o.assertNoMoreEvents(); // No initial value.

    subject.onNext(0);
    assertThat(o.takeNext()).isEqualTo(0);

    subject.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.emit(Lifecycle.Event.ON_PAUSE);
    lifecycle.emit(Lifecycle.Event.ON_STOP);
    lifecycle.emit(Lifecycle.Event.ON_DESTROY);

    subject.onNext(2);
    o.assertNoMoreEvents();
  }

  @Test
  @UiThreadTest
  public void observable_createDestroy() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    // Spin it up
    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    subject.as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle))).subscribe(o);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);

    Disposable d = o.takeSubscribe();
    o.assertNoMoreEvents(); // No initial value.

    subject.onNext(0);
    assertThat(o.takeNext()).isEqualTo(0);

    subject.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.emit(Lifecycle.Event.ON_PAUSE);
    lifecycle.emit(Lifecycle.Event.ON_STOP);
    lifecycle.emit(Lifecycle.Event.ON_DESTROY);

    subject.onNext(2);
    o.assertNoMoreEvents();
  }

  @Test
  @UiThreadTest
  public void observable_startStop() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    // Spin it up
    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    lifecycle.emit(Lifecycle.Event.ON_START);
    subject.as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle))).subscribe(o);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);

    Disposable d = o.takeSubscribe();
    o.assertNoMoreEvents(); // No initial value.

    subject.onNext(0);
    assertThat(o.takeNext()).isEqualTo(0);

    subject.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.emit(Lifecycle.Event.ON_PAUSE);

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_STOP);
    subject.onNext(2);
    o.assertNoMoreEvents();

    lifecycle.emit(Lifecycle.Event.ON_DESTROY);
  }

  @Test
  @UiThreadTest
  public void observable_resumePause() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    // Spin it up
    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);
    subject.as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle))).subscribe(o);

    Disposable d = o.takeSubscribe();
    o.assertNoMoreEvents(); // No initial value.

    subject.onNext(0);
    assertThat(o.takeNext()).isEqualTo(0);

    subject.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_PAUSE);
    subject.onNext(2);
    o.assertNoMoreEvents();

    lifecycle.emit(Lifecycle.Event.ON_STOP);
    lifecycle.emit(Lifecycle.Event.ON_DESTROY);
  }

  @Test
  @UiThreadTest
  public void observable_createPause() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    // Spin it up
    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    subject
        .as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle, Lifecycle.Event.ON_PAUSE)))
        .subscribe(o);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);

    Disposable d = o.takeSubscribe();
    o.assertNoMoreEvents(); // No initial value.

    subject.onNext(0);
    assertThat(o.takeNext()).isEqualTo(0);

    subject.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_PAUSE);
    subject.onNext(2);
    o.assertNoMoreEvents();

    lifecycle.emit(Lifecycle.Event.ON_STOP);
    lifecycle.emit(Lifecycle.Event.ON_DESTROY);
  }

  @Test
  @UiThreadTest
  public void observable_resumeDestroy() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    // Spin it up
    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);
    subject
        .as(
            autoDisposable(
                AndroidLifecycleScopeProvider.from(lifecycle, Lifecycle.Event.ON_DESTROY)))
        .subscribe(o);

    Disposable d = o.takeSubscribe();
    o.assertNoMoreEvents(); // No initial value.

    subject.onNext(0);
    assertThat(o.takeNext()).isEqualTo(0);

    subject.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.emit(Lifecycle.Event.ON_PAUSE);
    lifecycle.emit(Lifecycle.Event.ON_STOP);

    subject.onNext(2);
    assertThat(o.takeNext()).isEqualTo(2);

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_DESTROY);
    subject.onNext(3);
    o.assertNoMoreEvents();
  }

  @Test
  @UiThreadTest
  public void observable_offAfterPause_shouldStopOnStop() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);
    lifecycle.emit(Lifecycle.Event.ON_PAUSE);
    subject.as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle))).subscribe(o);

    Disposable d = o.takeSubscribe();

    subject.onNext(2);
    assertThat(o.takeNext()).isEqualTo(2);

    // We could resume again
    lifecycle.emit(Lifecycle.Event.ON_RESUME);
    subject.onNext(3);
    assertThat(o.takeNext()).isEqualTo(3);

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_STOP);
    subject.onNext(3);
    o.assertNoMoreEvents();
  }

  @Test
  @UiThreadTest
  public void observable_offAfterOnDestroyView_shouldStopOnDestroy() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);
    lifecycle.emit(Lifecycle.Event.ON_PAUSE);
    lifecycle.emit(Lifecycle.Event.ON_STOP);
    // In a CREATED state now but the next event will be destroy
    // This simulates subscribing in fragments' onDestroyView, where we want the subscription to
    // still dispose properly in onDestroy.
    subject.as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle))).subscribe(o);

    Disposable d = o.takeSubscribe();

    subject.onNext(2);
    assertThat(o.takeNext()).isEqualTo(2);

    // We should stop here
    lifecycle.emit(Lifecycle.Event.ON_DESTROY);
    subject.onNext(3);
    o.assertNoMoreEvents();
  }

  @Test
  public void observable_offMainThread_shouldFail() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> subject = PublishSubject.create();

    // Spin it up
    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);
    subject.as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle))).subscribe(o);

    Disposable d = o.takeSubscribe();
    Throwable t = o.takeError();
    assertThat(t).isInstanceOf(IllegalStateException.class);
    assertThat(t.getMessage()).contains("main thread");
    o.assertNoMoreEvents();
    assertThat(d.isDisposed()).isTrue();
  }

  @Test
  @UiThreadTest
  public void observable_offAfterDestroy_shouldFail() {
    final RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    final PublishSubject<Integer> subject = PublishSubject.create();

    TestLifecycleOwner lifecycle = TestLifecycleOwner.create();
    lifecycle.emit(Lifecycle.Event.ON_CREATE);
    lifecycle.emit(Lifecycle.Event.ON_START);
    lifecycle.emit(Lifecycle.Event.ON_RESUME);
    lifecycle.emit(Lifecycle.Event.ON_PAUSE);
    lifecycle.emit(Lifecycle.Event.ON_STOP);
    lifecycle.emit(Lifecycle.Event.ON_DESTROY);
    subject.as(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle))).subscribe(o);

    Disposable d = o.takeSubscribe();
    Throwable t = o.takeError();
    assertThat(t).isInstanceOf(LifecycleEndedException.class);
    o.assertNoMoreEvents();
    assertThat(d.isDisposed()).isTrue();
  }
}
