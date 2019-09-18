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
package com.ubercab.autodispose.rxlifecycle3;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.AutoDispose.autoDisposable;

import com.uber.autodispose.test.RecordingObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.junit.Test;

public class RxLifecycleInteropTest {

  private static final RecordingObserver.Logger LOGGER =
      message -> System.out.println(RxLifecycleInteropTest.class.getSimpleName() + ": " + message);

  private TestLifecycleProvider lifecycleProvider = new TestLifecycleProvider();

  @Test
  public void bindLifecycle_normalTermination_completeTheStream() {
    lifecycleProvider.emitCreate();
    TestObserver<Integer> o = new TestObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    Disposable d =
        source.to(autoDisposable(RxLifecycleInterop.from(lifecycleProvider))).subscribeWith(o);
    assertThat(o.hasSubscription()).isTrue();

    assertThat(source.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);
    source.onComplete();
    o.assertValues(1, 2);
    o.assertComplete();
    assertThat(d.isDisposed()).isFalse(); // Because it completed normally, was not disposed.
    assertThat(source.hasObservers()).isFalse();
  }

  @Test
  public void bindLifecycle_normalTermination_unsubscribe() {
    lifecycleProvider.emitCreate();
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    source.to(autoDisposable(RxLifecycleInterop.from(lifecycleProvider))).subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycleProvider.emitDestroy();
    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
  }

  @Test
  public void bindLifecycle_outsideLifecycleBound_unsubscribe() {
    lifecycleProvider.emitCreate();
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    lifecycleProvider.emitDestroy();
    source.to(autoDisposable(RxLifecycleInterop.from(lifecycleProvider))).subscribe(o);

    o.takeSubscribe();

    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse(); // Because RxLifecycle
    // treats OutsideLifecycleException as terminal event.
  }

  @Test
  public void bindUntilEvent_normalTermination_completeTheStream() {
    lifecycleProvider.emitCreate();
    TestObserver<Integer> o = new TestObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    Disposable d =
        source
            .to(
                autoDisposable(
                    RxLifecycleInterop.from(
                        lifecycleProvider, TestLifecycleProvider.Event.DESTROY)))
            .subscribeWith(o);
    assertThat(o.hasSubscription()).isTrue();

    assertThat(source.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);
    source.onComplete();
    o.assertValues(1, 2);
    o.assertComplete();
    assertThat(d.isDisposed()).isFalse(); // Because it completed normally, was not disposed.
    assertThat(source.hasObservers()).isFalse();
  }

  @Test
  public void bindUntilEvent_interruptedTermination_unsubscribe() {
    lifecycleProvider.emitCreate();
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    source
        .to(
            autoDisposable(
                RxLifecycleInterop.from(lifecycleProvider, TestLifecycleProvider.Event.DESTROY)))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycleProvider.emitDestroy();
    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
  }
}
