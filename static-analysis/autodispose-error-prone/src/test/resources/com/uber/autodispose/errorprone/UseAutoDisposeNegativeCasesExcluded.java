/*
 * Copyright (C) 2018. Uber Technologies
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

package com.uber.autodispose.errorprone;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction;
import com.uber.autodispose.lifecycle.LifecycleEndedException;
import com.uber.autodispose.lifecycle.LifecycleScopeProvider;
import com.uber.autodispose.lifecycle.TestLifecycleScopeProvider.TestLifecycle;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.annotations.Nullable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subscribers.TestSubscriber;
import org.reactivestreams.Subscriber;

import static com.uber.autodispose.AutoDispose.autoDisposable;

/**
 * Cases that use {@link AutoDispose} and should not fail the {@link UseAutoDispose} check.
 */
public class UseAutoDisposeNegativeCasesExcluded
    implements LifecycleScopeProvider<TestLifecycle> {

  private final BehaviorSubject<TestLifecycle> lifecycleSubject =
      BehaviorSubject.create();

  /**
   * @return a sequence of lifecycle events.
   */
  @CheckReturnValue public Observable<TestLifecycle> lifecycle() {
    return lifecycleSubject.hide();
  }

  /**
   * @return a sequence of lifecycle events. It's recommended to back this with a static instance to
   * avoid unnecessary object allocation.
   */
  @CheckReturnValue
  public CorrespondingEventsFunction<TestLifecycle> correspondingEvents() {
    return testLifecycle -> {
      switch (testLifecycle) {
        case STARTED:
          return TestLifecycle.STOPPED;
        case STOPPED:
          throw new LifecycleEndedException();
        default:
          throw new IllegalStateException("Unknown lifecycle event.");
      }
    };
  }

  /**
   * @return the last seen lifecycle event, or {@code null} if none.
   */
  @Nullable public TestLifecycle peekLifecycle() {
    return lifecycleSubject.getValue();
  }

  public void observable_subscribeWithAutoDispose() {
    Observable.just(1)
        .as(autoDisposable(this))
        .subscribe();
  }

  public void single_subscribeWithAutoDispose() {
    Single.just(1)
        .subscribe();
  }

  public void completable_subscribeWithAutoDispose() {
    Completable.complete()
        .subscribe();
  }

  public void maybe_subscribeWithAutoDispose() {
    Maybe.just(1)
        .subscribe();
  }

  public void flowable_subscribeWithAutoDispose() {
    Flowable.just(1)
        .subscribe();
  }

  public void parallelFlowable_subscribeWithAutoDispose() {
    Subscriber<Integer>[] subscribers = new Subscriber[] {};
    Flowable.just(1, 2)
        .parallel(2)
        .subscribe(subscribers);
  }

  public void observable_subscribeVoidSubscribe() {
    Observable.just(1)
        .subscribe(new TestObserver<>());
  }

  public void single_subscribeVoidSubscribe() {
    Single.just(1)
        .subscribe(new TestObserver<>());
  }

  public void completable_subscribeVoidSubscribe() {
    Completable.complete()
        .subscribe(new TestObserver<>());
  }

  public void maybe_subscribeVoidSubscribe() {
    Maybe.just(1)
        .subscribe(new TestObserver<>());
  }

  public void flowable_subscribeVoidSubscribe() {
    Flowable.just(1)
        .subscribe(new TestSubscriber<>());
  }

  public void observable_subscribeWith() {
    Observable.just(1)
        .subscribeWith(new TestObserver<>());
  }

  public void single_subscribeWith() {
    Single.just(1)
        .subscribeWith(new TestObserver<>());
  }

  public void completable_subscribeWith() {
    Completable.complete()
        .subscribeWith(new TestObserver<>());
  }

  public void maybe_subscribeWith() {
    Maybe.just(1)
        .subscribeWith(new TestObserver<>());
  }

  public void flowable_subscribeWith() {
    Flowable.just(1)
        .subscribeWith(new TestSubscriber<>());
  }
}
