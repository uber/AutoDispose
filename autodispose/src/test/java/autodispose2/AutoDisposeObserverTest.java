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
package autodispose2;

import static autodispose2.AutoDispose.autoDisposable;
import static autodispose2.TestUtil.outsideScopeProvider;
import static com.google.common.truth.Truth.assertThat;

import autodispose2.observers.AutoDisposingObserver;
import autodispose2.test.RecordingObserver;
import autodispose2.test.RxErrorsRule;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Rule;
import org.junit.Test;

public class AutoDisposeObserverTest extends PluginsMatrixTest {

  private static final RecordingObserver.Logger LOGGER =
      message -> System.out.println(AutoDisposeObserverTest.class.getSimpleName() + ": " + message);

  @Rule public final RxErrorsRule rule = new RxErrorsRule();

  public AutoDisposeObserverTest(boolean hideProxies) {
    super(hideProxies);
  }

  @Test
  public void autoDispose_withMaybe_normal() {
    TestObserver<Integer> o = new TestObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    Disposable d = source.to(autoDisposable(scope)).subscribeWith(o);
    assertThat(o.hasSubscription()).isTrue();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);
    source.onComplete();
    o.assertValues(1, 2);
    o.assertComplete();
    assertThat(d.isDisposed()).isFalse(); // Because it completed normally, was not disposed.
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withSuperClassGenerics_compilesFine() {
    Observable.just(new BClass())
        .to(autoDisposable(ScopeProvider.UNBOUND))
        .subscribe((Consumer<AClass>) aClass -> {});
  }

  @Test
  public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    source.to(autoDisposable(scope)).subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    scope.onComplete();
    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    CompletableSubject scope = CompletableSubject.create();
    ScopeProvider provider = TestUtil.makeProvider(scope);
    source.to(autoDisposable(provider)).subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    source.onNext(2);

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();
    assertThat(o.takeNext()).isEqualTo(2);

    scope.onComplete();
    source.onNext(3);

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();
  }

  @SuppressWarnings("NullAway")
  @Test
  public void verifyObserverDelegate() {
    final AtomicReference<Observer> atomicObserver = new AtomicReference<>();
    final AtomicReference<Observer> atomicAutoDisposingObserver = new AtomicReference<>();
    try {
      RxJavaPlugins.setOnObservableSubscribe(
          (source, observer) -> {
            if (atomicObserver.get() == null) {
              atomicObserver.set(observer);
            } else if (atomicAutoDisposingObserver.get() == null) {
              atomicAutoDisposingObserver.set(observer);
              RxJavaPlugins.setOnObservableSubscribe(null);
            }
            return observer;
          });
      Observable.just(1).to(autoDisposable(ScopeProvider.UNBOUND)).subscribe();

      assertThat(atomicAutoDisposingObserver.get()).isNotNull();
      assertThat(atomicAutoDisposingObserver.get()).isInstanceOf(AutoDisposingObserver.class);
      assertThat(((AutoDisposingObserver) atomicAutoDisposingObserver.get()).delegateObserver())
          .isNotNull();
      assertThat(((AutoDisposingObserver) atomicAutoDisposingObserver.get()).delegateObserver())
          .isSameInstanceAs(atomicObserver.get());
    } finally {
      RxJavaPlugins.reset();
    }
  }

  @Test
  public void verifyCancellation() {
    final AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    final ObservableEmitter<Integer>[] emitter = new ObservableEmitter[1];
    Observable<Integer> source =
        Observable.create(
            e -> {
              e.setCancellable(i::incrementAndGet);
              emitter[0] = e;
            });
    CompletableSubject scope = CompletableSubject.create();
    source.to(autoDisposable(scope)).subscribe();

    assertThat(i.get()).isEqualTo(0);
    assertThat(scope.hasObservers()).isTrue();

    emitter[0].onNext(1);

    scope.onComplete();
    emitter[0].onNext(2);

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(scope.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withScopeProviderCompleted_shouldNotReportDoubleSubscriptions() {
    TestObserver<Object> o =
        PublishSubject.create().to(autoDisposable(ScopeProvider.UNBOUND)).test();
    o.assertNoValues();
    o.assertNoErrors();

    rule.assertNoErrors();
  }

  @Test
  public void unbound_shouldStillPassValues() {
    PublishSubject<Integer> s = PublishSubject.create();
    TestObserver<Integer> o = s.to(autoDisposable(ScopeProvider.UNBOUND)).test();

    s.onNext(1);
    o.assertValue(1);
    o.dispose();
  }

  @Test
  public void autoDispose_outsideScope_withProviderAndNoOpPlugin_shouldFailSilently() {
    AutoDisposePlugins.setOutsideScopeHandler(e -> {});
    ScopeProvider provider = outsideScopeProvider();
    PublishSubject<Integer> source = PublishSubject.create();
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
    TestObserver<Integer> o = PublishSubject.<Integer>create().to(autoDisposable(provider)).test();

    o.assertNoValues();
    o.assertError(
        throwable ->
            throwable instanceof IllegalStateException
                && throwable.getCause() instanceof OutsideScopeException);
  }

  @Test
  public void hideProxies() {
    ObservableSubscribeProxy proxy = Observable.never().to(autoDisposable(ScopeProvider.UNBOUND));
    // If hideProxies is disabled, the underlying return should be the direct AutoDispose type.
    if (hideProxies) {
      assertThat(proxy).isNotInstanceOf(Observable.class);
    } else {
      assertThat(proxy).isInstanceOf(AutoDisposeObservable.class);
    }
  }
}
