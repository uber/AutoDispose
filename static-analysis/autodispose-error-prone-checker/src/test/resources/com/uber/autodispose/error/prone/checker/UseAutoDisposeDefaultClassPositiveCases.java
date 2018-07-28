/*
 * Copyright (c) 2018. Uber Technologies
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

package com.uber.autodispose.error.prone.checker;

import com.uber.autodispose.lifecycle.CorrespondingEventsFunction;
import com.uber.autodispose.lifecycle.LifecycleEndedException;
import com.uber.autodispose.lifecycle.LifecycleScopeProvider;
import com.uber.autodispose.lifecycle.LifecycleScopes;
import com.uber.autodispose.lifecycle.TestLifecycleScopeProvider;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;

/**
 * Cases that don't use autodispose and should fail the {@link UseAutoDispose} check.
 */
public class UseAutoDisposeDefaultClassPositiveCases
    implements LifecycleScopeProvider<TestLifecycleScopeProvider.TestLifecycle> {

  private final BehaviorSubject<TestLifecycleScopeProvider.TestLifecycle> lifecycleSubject =
      BehaviorSubject.create();

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
  @CheckReturnValue
  public CorrespondingEventsFunction<TestLifecycleScopeProvider.TestLifecycle> correspondingEvents() {
    return new CorrespondingEventsFunction<TestLifecycleScopeProvider.TestLifecycle>() {
      @Override public TestLifecycleScopeProvider.TestLifecycle apply(
          TestLifecycleScopeProvider.TestLifecycle testLifecycle) {
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

  @Override public Maybe<?> requestScope() throws Exception {
    return LifecycleScopes.resolveScopeFromLifecycle(this);
  }

  public void observable_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Observable.empty().subscribe();
  }

  public void single_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Single.just(true).subscribe();
  }

  public void completable_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Completable.complete().subscribe();
  }

  public void maybe_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Maybe.empty().subscribe();
  }

  public void flowable_subscribeWithoutAutoDispose() {
    // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
    Flowable.empty().subscribe();
  }

  public void parallelFlowable_subscribeWithoutAutoDispose() {
    Subscriber<Integer>[] subscribers = new Subscriber[] {};
    Flowable.just(1, 2)
        .parallel(2)
        // BUG: Diagnostic contains: Always apply an AutoDispose scope before subscribing within defined scoped elements.
        .subscribe(subscribers);
  }
}
