/*
 * Copyright (C) 2018. Uber Technologies
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

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposePlugins;
import com.uber.autodispose.RxErrorsRule;
import com.uber.autodispose.test.RecordingObserver;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.lifecycle.TestUtil.makeLifecycleProvider;

public class LifecycleScopeProviderObservableTest {

  private static final RecordingObserver.Logger LOGGER = new RecordingObserver.Logger() {
    @Override public void log(String message) {
      System.out.println(LifecycleScopeProviderObservableTest.class.getSimpleName() + ": " + message);
    }
  };

  @Rule public RxErrorsRule rule = new RxErrorsRule();

  @After public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test public void autoDispose_withLifecycleProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    source.as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.onNext(1);
    source.onNext(2);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();
    assertThat(o.takeNext()).isEqualTo(2);

    lifecycle.onNext(3);
    source.onNext(3);

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    Observable.just(1)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(o);

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    Observable.just(1)
        .as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(o);

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleEndedException.class);
  }

}
