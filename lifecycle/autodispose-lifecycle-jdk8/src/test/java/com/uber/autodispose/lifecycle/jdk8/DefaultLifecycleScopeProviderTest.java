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

package com.uber.autodispose.lifecycle.jdk8;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction;
import com.uber.autodispose.lifecycle.LifecycleEndedException;
import com.uber.autodispose.test.RecordingObserver;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class DefaultLifecycleScopeProviderTest {

  enum LifecycleEvent {
    START, STOP
  }

  /**
   * A thing with a lifecycle that uses the default implementation of {@link #requestScope()}.
   */
  static class ThingWithALifecycle implements DefaultLifecycleScopeProvider<LifecycleEvent> {

    BehaviorSubject<LifecycleEvent> lifecycle = BehaviorSubject.createDefault(LifecycleEvent.START);

    @Override public Observable<LifecycleEvent> lifecycle() {
      return lifecycle.hide();
    }

    @Override public CorrespondingEventsFunction<LifecycleEvent> correspondingEvents() {
      return event -> {
        switch (event) {
          case START:
            return LifecycleEvent.STOP;
          case STOP:
            throw new LifecycleEndedException("Ended!");
          default:
            throw new IllegalStateException("This can never happen");
        }
      };
    }

    @Override public LifecycleEvent peekLifecycle() {
      return lifecycle.getValue();
    }
  }

  @Test public void smokeTest() {
    RecordingObserver<Integer> o = new RecordingObserver<>(System.out::println);
    PublishSubject<Integer> source = PublishSubject.create();
    ThingWithALifecycle provider = new ThingWithALifecycle();
    BehaviorSubject<LifecycleEvent> lifecycle = provider.lifecycle;
    source.as(AutoDispose.autoDisposable(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.onNext(LifecycleEvent.START);
    source.onNext(2);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();
    assertThat(o.takeNext()).isEqualTo(2);

    lifecycle.onNext(LifecycleEvent.STOP);
    source.onNext(3);

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }
}
