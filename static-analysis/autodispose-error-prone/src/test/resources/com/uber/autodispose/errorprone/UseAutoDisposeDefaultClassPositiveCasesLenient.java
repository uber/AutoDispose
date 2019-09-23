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
package com.uber.autodispose.errorprone;

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
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.util.function.Function;
import org.reactivestreams.Subscriber;

/** Cases that don't use autodispose and should fail the {@link UseAutoDispose} check. */
public class UseAutoDisposeDefaultClassPositiveCasesLenient
    implements LifecycleScopeProvider<TestLifecycle> {

  private final BehaviorSubject<TestLifecycle> lifecycleSubject = BehaviorSubject.create();

  /** @return a sequence of lifecycle events. */
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

  /** @return the last seen lifecycle event, or {@code null} if none. */
  @Nullable
  public TestLifecycle peekLifecycle() {
    return lifecycleSubject.getValue();
  }

  public void observable_subscribeWithoutAutoDispose() {
    Observable.empty()
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe();
  }

  public void single_subscribeWithoutAutoDispose() {
    Single.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe();
  }

  public void completable_subscribeWithoutAutoDispose() {
    Completable.complete()
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe();
  }

  public void maybe_subscribeWithoutAutoDispose() {
    Maybe.empty()
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe();
  }

  public void flowable_subscribeWithoutAutoDispose() {
    Flowable.empty()
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe();
  }

  public void parallelFlowable_subscribeWithoutAutoDispose() {
    Subscriber<Integer>[] subscribers = new Subscriber[] {};
    Flowable.just(1, 2)
        .parallel(2)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe(subscribers);
  }

  public void observable_subscribeVoidSubscribe_withoutAutoDispose() {
    Observable.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe(new TestObserver<>());
  }

  public void single_subscribeVoidSubscribe_withoutAutoDispose() {
    Single.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe(new TestObserver<>());
  }

  public void completable_subscribeVoidSubscribe_withoutAutoDispose() {
    Completable.complete()
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe(new TestObserver<>());
  }

  public void maybe_subscribeVoidSubscribe_withoutAutoDispose() {
    Maybe.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe(new TestObserver<>());
  }

  public void flowable_subscribeVoidSubscribe_withoutAutoDispose() {
    Flowable.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribe(new TestSubscriber<>());
  }

  public void observable_subscribeWith_notKeepingResult() {
    Observable.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribeWith(new TestObserver<>());
  }

  public void single_subscribeWith_notKeepingResult() {
    Single.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribeWith(new TestObserver<>());
  }

  public void completable_subscribeWith_notKeepingResult() {
    Completable.complete()
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribeWith(new TestObserver<>());
  }

  public void maybe_subscribeWith_notKeepingResult() {
    Maybe.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribeWith(new TestObserver<>());
  }

  public void flowable_subscribeWith_notKeepingResult() {
    Flowable.just(1)
        // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
        // the Disposable instance manually and enable lenient mode.
        .subscribeWith(new TestSubscriber<>());
  }

  // subscribeWith only works IFF the argument passed implements Disposable
  public void subscribeWithOnlyDisposable() {
    Observer<Integer> o =
        Observable.just(1)
            // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
            // the Disposable instance manually and enable lenient mode.
            .subscribeWith(
                new Observer<Integer>() {
                  @Override
                  public void onSubscribe(Disposable d) {}

                  @Override
                  public void onNext(Integer integer) {}

                  @Override
                  public void onError(Throwable e) {}

                  @Override
                  public void onComplete() {}
                });
  }

  // subscribeWith only works IFF the argument passed implements Disposable
  // Method references in general are very tricky to get right, because we're relatively limited in
  // being able to detect how the return type is used.
  public void subscribeWithOnlyDisposable_asReference() {
    Observable<Integer> source = Observable.just(1);
    // BUG: Diagnostic contains: Missing Disposable handling: Apply AutoDispose or cache
    // the Disposable instance manually and enable lenient mode.
    Observer<Integer> o = methodReferencable(source::subscribeWith);
  }

  Observer<Integer> methodReferencable(Function<Observer<Integer>, Observer<Integer>> func) {
    return func.apply(
        new Observer<Integer>() {
          @Override
          public void onSubscribe(Disposable d) {}

          @Override
          public void onNext(Integer integer) {}

          @Override
          public void onError(Throwable e) {}

          @Override
          public void onComplete() {}
        });
  }
}
