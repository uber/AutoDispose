/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.errorprone;

import static autodispose2.AutoDispose.autoDisposable;

import autodispose2.AutoDispose;
import autodispose2.lifecycle.CorrespondingEventsFunction;
import autodispose2.lifecycle.LifecycleEndedException;
import autodispose2.lifecycle.LifecycleScopeProvider;
import autodispose2.lifecycle.TestLifecycleScopeProvider.TestLifecycle;
import io.reactivex.rxjava3.annotations.CheckReturnValue;
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.reactivestreams.Subscriber;

/** Cases that use {@link AutoDispose} and should not fail the {@link UseAutoDispose} check. */
public class UseAutoDisposeNegativeCases implements LifecycleScopeProvider<TestLifecycle> {

  private final BehaviorSubject<TestLifecycle> lifecycleSubject = BehaviorSubject.create();

  /**
   * @return a sequence of lifecycle events.
   */
  @CheckReturnValue
  public Observable<TestLifecycle> lifecycle() {
    return lifecycleSubject.hide();
  }

  /**
   * @return a sequence of lifecycle events. It's recommended to back this with a static instance to
   *     avoid unnecessary object allocation.
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
  @Nullable
  public TestLifecycle peekLifecycle() {
    return lifecycleSubject.getValue();
  }

  public void observable_subscribeWithAutoDispose() {
    Observable.just(1).to(autoDisposable(this)).subscribe();
  }

  public void single_subscribeWithAutoDispose() {
    Single.just(1).to(autoDisposable(this)).subscribe();
  }

  public void completable_subscribeWithAutoDispose() {
    Completable.complete().to(autoDisposable(this)).subscribe();
  }

  public void maybe_subscribeWithAutoDispose() {
    Maybe.just(1).to(autoDisposable(this)).subscribe();
  }

  public void flowable_subscribeWithAutoDispose() {
    Flowable.just(1).to(autoDisposable(this)).subscribe();
  }

  public void parallelFlowable_subscribeWithAutoDispose() {
    Subscriber<Integer>[] subscribers = new Subscriber[] {};
    Flowable.just(1, 2).parallel(2).to(autoDisposable(this)).subscribe(subscribers);
  }

  public void observable_subscribeVoidSubscribe() {
    Observable.just(1).to(autoDisposable(this)).subscribe(new TestObserver<>());
  }

  public void single_subscribeVoidSubscribe() {
    Single.just(1).to(autoDisposable(this)).subscribe(new TestObserver<>());
  }

  public void completable_subscribeVoidSubscribe() {
    Completable.complete().to(autoDisposable(this)).subscribe(new TestObserver<>());
  }

  public void maybe_subscribeVoidSubscribe() {
    Maybe.just(1).to(autoDisposable(this)).subscribe(new TestObserver<>());
  }

  public void flowable_subscribeVoidSubscribe() {
    Flowable.just(1).to(autoDisposable(this)).subscribe(new TestSubscriber<>());
  }

  public void observable_subscribeWith() {
    Observable.just(1).to(autoDisposable(this)).subscribeWith(new TestObserver<>());
  }

  public void single_subscribeWith() {
    Single.just(1).to(autoDisposable(this)).subscribeWith(new TestObserver<>());
  }

  public void completable_subscribeWith() {
    Completable.complete().to(autoDisposable(this)).subscribeWith(new TestObserver<>());
  }

  public void maybe_subscribeWith() {
    Maybe.just(1).to(autoDisposable(this)).subscribeWith(new TestObserver<>());
  }

  public void flowable_subscribeWith() {
    Flowable.just(1).to(autoDisposable(this)).subscribeWith(new TestSubscriber<>());
  }
}
