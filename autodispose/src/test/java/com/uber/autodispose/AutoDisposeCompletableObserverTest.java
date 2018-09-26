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

package com.uber.autodispose;

import com.uber.autodispose.observers.AutoDisposingCompletableObserver;
import com.uber.autodispose.test.RecordingObserver;
import com.uber.autodispose.test.RxErrorsRule;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.CompletableSubject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.TestUtil.makeProvider;
import static com.uber.autodispose.TestUtil.outsideScopeProvider;

public class AutoDisposeCompletableObserverTest {

  private static final RecordingObserver.Logger LOGGER = message -> System.out.println(AutoDisposeCompletableObserverTest.class.getSimpleName() + ": " + message);

  @Rule public RxErrorsRule rule = new RxErrorsRule();

  @Test public void autoDispose_withMaybe_normal() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    CompletableSubject source = CompletableSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    source.as(autoDisposable(scope))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    // Got the event
    source.onComplete();
    o.assertOnComplete();

    // Nothing more, scope disposed too
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    CompletableSubject source = CompletableSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    source.as(autoDisposable(scope))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    // Lifecycle ends
    scope.onComplete();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();

    // Event if upstream emits, no one is listening
    source.onComplete();
    o.assertNoMoreEvents();
  }

  @Test public void autoDispose_withProvider_completion() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    CompletableSubject source = CompletableSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.as(autoDisposable(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onComplete();
    o.assertOnComplete();

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    CompletableSubject source = CompletableSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.as(autoDisposable(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    scope.onComplete();

    // All disposed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();

    // No one is listening
    source.onComplete();
    o.assertNoMoreEvents();
  }

  @Test public void verifyObserverDelegate() {
    final AtomicReference<CompletableObserver> atomicObserver = new AtomicReference<>();
    final AtomicReference<CompletableObserver> atomicAutoDisposingObserver = new AtomicReference<>();
    try {
      RxJavaPlugins.setOnCompletableSubscribe((source, observer) -> {
        if (atomicObserver.get() == null) {
          atomicObserver.set(observer);
        } else if (atomicAutoDisposingObserver.get() == null) {
          atomicAutoDisposingObserver.set(observer);
          RxJavaPlugins.setOnObservableSubscribe(null);
        }
        return observer;
      });
      Completable.complete()
          .as(autoDisposable(ScopeProvider.UNBOUND))
          .subscribe();

      assertThat(atomicAutoDisposingObserver.get()).isNotNull();
      assertThat(atomicAutoDisposingObserver.get()).isInstanceOf(AutoDisposingCompletableObserver.class);
      assertThat(((AutoDisposingCompletableObserver) atomicAutoDisposingObserver.get()).delegateObserver()).isNotNull();
      assertThat(((AutoDisposingCompletableObserver) atomicAutoDisposingObserver.get()).delegateObserver()).isSameAs(
          atomicObserver.get());
    } finally {
      RxJavaPlugins.reset();
    }
  }

  @Test public void verifyCancellation() {
    final AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    Completable source = Completable.create(e -> e.setCancellable(i::incrementAndGet));
    CompletableSubject scope = CompletableSubject.create();
    source.as(autoDisposable(scope))
        .subscribe();

    assertThat(i.get()).isEqualTo(0);
    assertThat(scope.hasObservers()).isTrue();

    scope.onComplete();

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withScopeProviderCompleted_shouldNotReportDoubleSubscriptions() {
    TestObserver<Void> o = CompletableSubject.create()
        .as(autoDisposable(ScopeProvider.UNBOUND))
        .test();
    o.assertNoValues();
    o.assertNoErrors();

    rule.assertNoErrors();
  }

  @Test public void unbound_shouldStillPassValues() {
    TestObserver<Void> o = CompletableSubject.create()
        .as(autoDisposable(ScopeProvider.UNBOUND))
        .test();

    o.onComplete();
    o.assertComplete();
  }

  @Test public void autoDispose_outsideScope_withProviderAndNoOpPlugin_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> { });
    ScopeProvider provider = outsideScopeProvider();
    CompletableSubject source = CompletableSubject.create();
    TestObserver<Void> o = source.as(autoDisposable(provider))
        .test();

    assertThat(source.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test public void autoDispose_outsideScope_withProviderAndPlugin_shouldFailWithWrappedExp() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> {
      // Wrap in an IllegalStateException so we can verify this is the exception we see on the
      // other side
      throw new IllegalStateException(e);
    });
    ScopeProvider provider = outsideScopeProvider();
    TestObserver<Void> o = CompletableSubject.create()
        .as(autoDisposable(provider))
        .test();

    o.assertNoValues();
    o.assertError(throwable -> throwable instanceof IllegalStateException && throwable.getCause() instanceof OutsideScopeException);
  }
}
