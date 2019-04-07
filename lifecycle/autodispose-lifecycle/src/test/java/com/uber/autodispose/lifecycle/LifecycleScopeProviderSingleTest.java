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
package com.uber.autodispose.lifecycle;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.lifecycle.TestUtil.makeLifecycleProvider;

import com.uber.autodispose.AutoDisposePlugins;
import com.uber.autodispose.OutsideScopeException;
import com.uber.autodispose.test.RecordingObserver;
import com.uber.autodispose.test.RxErrorsRule;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.SingleSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LifecycleScopeProviderSingleTest {

  private static final RecordingObserver.Logger LOGGER =
      message ->
          System.out.println(
              LifecycleScopeProviderSingleTest.class.getSimpleName() + ": " + message);

  @Rule public RxErrorsRule rule = new RxErrorsRule();

  @Before
  @After
  public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test
  public void autoDispose_withLifecycleProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    SingleSubject<Integer> source = SingleSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    source.as(autoDisposable(provider)).subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onNext(1);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onSuccess(3);
    o.takeSuccess();

    // All cleaned up
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withLifecycleProvider_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    SingleSubject<Integer> source = SingleSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    source.as(autoDisposable(provider)).subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onNext(1);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    // Lifecycle ends
    lifecycle.onNext(3);
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();

    // No one is listening even if upstream finally does emit
    source.onSuccess(3);
    o.assertNoMoreEvents();
  }

  @Test
  public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    Single.just(1).as(autoDisposable(provider)).subscribe(o);

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test
  public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    Single.just(1).as(autoDisposable(provider)).subscribe(o);

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleEndedException.class);
  }

  @Test
  public void autoDispose_withProviderAndNoOpPlugin_withoutStarting_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> {});
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    SingleSubject<Integer> source = SingleSubject.create();
    TestObserver<Integer> o = source.as(autoDisposable(provider)).test();

    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test
  public void autoDispose_withProviderAndNoOpPlugin_afterEnding_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(
        e -> {
          // Noop
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    SingleSubject<Integer> source = SingleSubject.create();
    TestObserver<Integer> o = source.as(autoDisposable(provider)).test();

    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test
  public void autoDispose_withProviderAndPlugin_withoutStarting_shouldFailWithExp() {
    AutoDisposePlugins.setOutsideScopeHandler(
        e -> {
          // Wrap in an IllegalStateException so we can verify this is the exception we see on the
          // other side
          throw new IllegalStateException(e);
        });
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    SingleSubject<Integer> source = SingleSubject.create();
    TestObserver<Integer> o = source.as(autoDisposable(provider)).test();

    o.assertNoValues();
    o.assertError(
        throwable ->
            throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideScopeException);
  }
}
