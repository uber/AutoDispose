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

import com.uber.autodispose.observers.AutoDisposingSingleObserver;
import com.uber.autodispose.test.RecordingObserver;
import com.uber.autodispose.test.RxErrorsRule;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.SingleSubject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.TestUtil.makeProvider;
import static com.uber.autodispose.TestUtil.outsideScopeProvider;

public class AutoDisposeSingleObserverTest {

  private static final RecordingObserver.Logger LOGGER =
      message -> System.out.println(AutoDisposeSingleObserverTest.class.getSimpleName() + ": " + message);

  @Rule public RxErrorsRule rule = new RxErrorsRule();

  @Test public void autoDispose_withMaybe_normal() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    SingleSubject<Integer> source = SingleSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    source.as(AutoDispose.<Integer>autoDisposable(scope))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    // Got the event
    source.onSuccess(1);
    assertThat(o.takeSuccess()).isEqualTo(1);

    // Nothing more, scope disposed too
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withSuperClassGenerics_compilesFine() {
    Single.just(new BClass())
        .as(AutoDispose.<BClass>autoDisposable(ScopeProvider.UNBOUND))
        .subscribe((Consumer<AClass>) aClass -> {

        });
  }

  @Test public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    SingleSubject<Integer> source = SingleSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    source.as(AutoDispose.<Integer>autoDisposable(scope))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    // Lifecycle ends
    scope.onComplete();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();

    // Event if upstream emits, no one is listening
    source.onSuccess(2);
    o.assertNoMoreEvents();
  }

  @Test public void autoDispose_withProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    SingleSubject<Integer> source = SingleSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onSuccess(3);
    o.takeSuccess();

    // All cleaned up
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withProvider_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    SingleSubject<Integer> source = SingleSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.as(AutoDispose.<Integer>autoDisposable(provider))
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    // Lifecycle ends
    scope.onComplete();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();

    // No one is listening even if upstream finally does emit
    source.onSuccess(3);
    o.assertNoMoreEvents();
  }

  @Test public void verifyObserverDelegate() {
    final AtomicReference<SingleObserver> atomicObserver = new AtomicReference<>();
    final AtomicReference<SingleObserver> atomicAutoDisposingObserver = new AtomicReference<>();
    try {
      RxJavaPlugins.setOnSingleSubscribe((source, observer) -> {
        if (atomicObserver.get() == null) {
          atomicObserver.set(observer);
        } else if (atomicAutoDisposingObserver.get() == null) {
          atomicAutoDisposingObserver.set(observer);
          RxJavaPlugins.setOnObservableSubscribe(null);
        }
        return observer;
      });
      Single.just(1)
          .as(AutoDispose.<Integer>autoDisposable(ScopeProvider.UNBOUND))
          .subscribe();

      assertThat(atomicAutoDisposingObserver.get()).isNotNull();
      assertThat(atomicAutoDisposingObserver.get()).isInstanceOf(AutoDisposingSingleObserver.class);
      assertThat(((AutoDisposingSingleObserver) atomicAutoDisposingObserver.get()).delegateObserver()).isNotNull();
      assertThat(((AutoDisposingSingleObserver) atomicAutoDisposingObserver.get()).delegateObserver()).isSameAs(
          atomicObserver.get());
    } finally {
      RxJavaPlugins.reset();
    }
  }

  @Test public void verifyCancellation() {
    final AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    Single<Integer> source = Single.create(e -> e.setCancellable(i::incrementAndGet));
    CompletableSubject scope = CompletableSubject.create();
    source.as(AutoDispose.<Integer>autoDisposable(scope))
        .subscribe();

    assertThat(i.get()).isEqualTo(0);
    assertThat(scope.hasObservers()).isTrue();

    scope.onComplete();

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test public void autoDispose_withScopeProviderCompleted_shouldNotReportDoubleSubscriptions() {
    TestObserver<Object> o = SingleSubject.create()
        .as(AutoDispose.autoDisposable(ScopeProvider.UNBOUND))
        .test();
    o.assertNoValues();
    o.assertNoErrors();

    rule.assertNoErrors();
  }

  @Test public void unbound_shouldStillPassValues() {
    SingleSubject<Integer> s = SingleSubject.create();
    TestObserver<Integer> o = s.as(AutoDispose.<Integer>autoDisposable(ScopeProvider.UNBOUND))
        .test();

    s.onSuccess(1);
    o.assertValue(1);
  }

  @Test public void autoDispose_outsideScope_withProviderAndNoOpPlugin_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> { });
    ScopeProvider provider = outsideScopeProvider();
    SingleSubject<Integer> source = SingleSubject.create();
    TestObserver<Integer> o = source.as(AutoDispose.<Integer>autoDisposable(provider))
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
    TestObserver<Integer> o = SingleSubject.<Integer>create().as(AutoDispose.<Integer>autoDisposable(provider))
        .test();

    o.assertNoValues();
    o.assertError(throwable -> throwable instanceof IllegalStateException && throwable.getCause() instanceof OutsideScopeException);
  }
}
