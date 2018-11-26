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

package com.uber.autodispose.errorprone;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.errorprone.UseAutoDispose;
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction;
import com.uber.autodispose.lifecycle.LifecycleEndedException;
import com.uber.autodispose.lifecycle.LifecycleScopeProvider;
import com.uber.autodispose.lifecycle.LifecycleScopes;
import com.uber.autodispose.lifecycle.TestLifecycleScopeProvider;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.annotations.Nullable;
import io.reactivex.subjects.BehaviorSubject;
import org.reactivestreams.Subscriber;

import static com.uber.autodispose.AutoDispose.autoDisposable;

/**
 * Cases that use {@link AutoDispose} and should not fail the {@link UseAutoDispose} check.
 */
public class UseAutoDisposeNegativeCases implements LifecycleScopeProvider<TestLifecycleScopeProvider.TestLifecycle> {

  private final BehaviorSubject<TestLifecycleScopeProvider.TestLifecycle> lifecycleSubject = BehaviorSubject.create();

  /**
   * @return a sequence of lifecycle events.
   */
  @CheckReturnValue public Observable<TestLifecycleScopeProvider.TestLifecycle> lifecycle() {
    return lifecycleSubject.hide();
  }

  /**
   * @return a sequence of lifecycle events. It's recommended to back this with a static instance to
   * avoid unnecessary object allocation.
   */
  @CheckReturnValue public CorrespondingEventsFunction<TestLifecycleScopeProvider.TestLifecycle> correspondingEvents() {
    return new CorrespondingEventsFunction<TestLifecycleScopeProvider.TestLifecycle>() {
      @Override
      public TestLifecycleScopeProvider.TestLifecycle apply(TestLifecycleScopeProvider.TestLifecycle testLifecycle) {
        switch (testLifecycle) {
          case STARTED:
            return TestLifecycleScopeProvider.TestLifecycle.STOPPED;
          case STOPPED:
            throw new LifecycleEndedException();
          default:
            throw new IllegalStateException("Unknown lifecycle event.");
        }
      }
    };
  }

  /**
   * @return the last seen lifecycle event, or {@code null} if none.
   */
  @Nullable public TestLifecycleScopeProvider.TestLifecycle peekLifecycle() {
    return lifecycleSubject.getValue();
  }

  @Override public CompletableSource requestScope() throws Exception {
    return LifecycleScopes.resolveScopeFromLifecycle(this);
  }

  public void observable_subscribeWithAutoDispose() {
    Observable.just(1)
        .as(autoDisposable(this))
        .subscribe();
  }

  public void single_subscribeWithAutoDispose() {
    Single.just(true)
        .as(autoDisposable(this))
        .subscribe();
  }

  public void completable_subscribeWithAutoDispose() {
    Completable.complete()
        .as(autoDisposable(this))
        .subscribe();
  }

  public void maybe_subscribeWithAutoDispose() {
    Maybe.just(1)
        .as(autoDisposable(this))
        .subscribe();
  }

  public void flowable_subscribeWithAutoDispose() {
    Flowable.just(1)
        .as(autoDisposable(this))
        .subscribe();
  }

  public void parallelFlowable_subscribeWithAutoDispose() {
    Subscriber<Integer>[] subscribers = new Subscriber[] {};
    Flowable.just(1, 2)
        .parallel(2)
        .as(autoDisposable(this))
        .subscribe(subscribers);
  }
}
