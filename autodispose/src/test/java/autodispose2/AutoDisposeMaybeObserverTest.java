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
package autodispose2;

import static com.google.common.truth.Truth.assertThat;
import static autodispose2.AutoDispose.autoDisposable;
import static autodispose2.TestUtil.makeProvider;
import static autodispose2.TestUtil.outsideScopeProvider;

import autodispose2.observers.AutoDisposingMaybeObserver;
import autodispose2.test.RecordingObserver;
import autodispose2.test.RxErrorsRule;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Rule;
import org.junit.Test;

public class AutoDisposeMaybeObserverTest extends PluginsMatrixTest {

  private static final RecordingObserver.Logger LOGGER =
      message ->
          System.out.println(AutoDisposeMaybeObserverTest.class.getSimpleName() + ": " + message);

  @Rule public RxErrorsRule rule = new RxErrorsRule();

  public AutoDisposeMaybeObserverTest(boolean hideProxies) {
    super(hideProxies);
  }

  @Test
  public void autoDispose_withMaybe_normal() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    MaybeSubject<Integer> source = MaybeSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    source.to(autoDisposable(scope)).subscribe(o);
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

  @Test
  public void autoDispose_withSuperClassGenerics_compilesFine() {
    Maybe.just(new BClass())
        .to(autoDisposable(ScopeProvider.UNBOUND))
        .subscribe((Consumer<AClass>) aClass -> {});
  }

  @Test
  public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    MaybeSubject<Integer> source = MaybeSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    source.to(autoDisposable(scope)).subscribe(o);
    source.to(autoDisposable(scope)).subscribe(integer -> {});

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

  @Test
  public void autoDispose_withProvider_success() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    MaybeSubject<Integer> source = MaybeSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.to(autoDisposable(provider)).subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onSuccess(3);
    o.takeSuccess();

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider_completion() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    MaybeSubject<Integer> source = MaybeSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.to(autoDisposable(provider)).subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onComplete();
    o.assertOnComplete();

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    MaybeSubject<Integer> source = MaybeSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.to(autoDisposable(provider)).subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    scope.onComplete();

    // All disposed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();

    // No one is listening
    source.onSuccess(3);
    o.assertNoMoreEvents();
  }

  @Test
  public void verifyObserverDelegate() {
    final AtomicReference<MaybeObserver> atomicObserver = new AtomicReference<>();
    final AtomicReference<MaybeObserver> atomicAutoDisposingObserver = new AtomicReference<>();
    try {
      RxJavaPlugins.setOnMaybeSubscribe(
          (source, observer) -> {
            if (atomicObserver.get() == null) {
              atomicObserver.set(observer);
            } else if (atomicAutoDisposingObserver.get() == null) {
              atomicAutoDisposingObserver.set(observer);
              RxJavaPlugins.setOnObservableSubscribe(null);
            }
            return observer;
          });
      Maybe.just(1).to(autoDisposable(ScopeProvider.UNBOUND)).subscribe();

      assertThat(atomicAutoDisposingObserver.get()).isNotNull();
      assertThat(atomicAutoDisposingObserver.get()).isInstanceOf(AutoDisposingMaybeObserver.class);
      assertThat(
              ((AutoDisposingMaybeObserver) atomicAutoDisposingObserver.get()).delegateObserver())
          .isNotNull();
      assertThat(
              ((AutoDisposingMaybeObserver) atomicAutoDisposingObserver.get()).delegateObserver())
          .isSameInstanceAs(atomicObserver.get());
    } finally {
      RxJavaPlugins.reset();
    }
  }

  @Test
  public void verifyCancellation() {
    final AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    Maybe<Integer> source = Maybe.create(e -> e.setCancellable(i::incrementAndGet));
    CompletableSubject scope = CompletableSubject.create();
    source.to(autoDisposable(scope)).subscribe();

    assertThat(i.get()).isEqualTo(0);
    assertThat(scope.hasObservers()).isTrue();

    scope.onComplete();

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withScopeProviderCompleted_shouldNotReportDoubleSubscriptions() {
    TestObserver<Object> o = MaybeSubject.create().to(autoDisposable(ScopeProvider.UNBOUND)).test();
    o.assertNoValues();
    o.assertNoErrors();

    rule.assertNoErrors();
  }

  @Test
  public void unbound_shouldStillPassValues() {
    MaybeSubject<Integer> s = MaybeSubject.create();
    TestObserver<Integer> o = s.to(autoDisposable(ScopeProvider.UNBOUND)).test();

    s.onSuccess(1);
    o.assertValue(1);
  }

  @Test
  public void autoDispose_outsideScope_withProviderAndNoOpPlugin_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> {});
    ScopeProvider provider = outsideScopeProvider();
    MaybeSubject<Integer> source = MaybeSubject.create();
    TestObserver<Integer> o = source.to(autoDisposable(provider)).test();

    assertThat(source.hasObservers()).isFalse();
    o.assertNoValues();
    o.assertNoErrors();
  }

  @Test
  public void autoDispose_outsideScope_withProviderAndPlugin_shouldFailWithWrappedExp() {
    AutoDisposePlugins.setOutsideScopeHandler(
        e -> {
          // Wrap in an IllegalStateException so we can verify this is the exception we see on the
          // other side
          throw new IllegalStateException(e);
        });
    ScopeProvider provider = outsideScopeProvider();
    TestObserver<Integer> o = MaybeSubject.<Integer>create().to(autoDisposable(provider)).test();

    o.assertNoValues();
    o.assertError(
        throwable ->
            throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideScopeException);
  }

  @Test
  public void hideProxies() {
    MaybeSubscribeProxy proxy = Maybe.never().to(autoDisposable(ScopeProvider.UNBOUND));
    // If hideProxies is disabled, the underlying return should be the direct AutoDispose type.
    if (hideProxies) {
      assertThat(proxy).isNotInstanceOf(Maybe.class);
    } else {
      assertThat(proxy).isInstanceOf(AutoDisposeMaybe.class);
    }
  }
}
