package com.uber.autodispose;

import hu.akarnokd.rxjava2.subjects.MaybeSubject;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class AutoDisposeObserverTest {

  @Test
  public void autoDispose_withMaybe_normal() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    AutoDisposingObserver<Integer> auto =
        (AutoDisposingObserver<Integer>) AutoDispose.observable(lifecycle)
            .around(o);
    source.subscribe(auto);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    source.onNext(2);
    source.onComplete();
    assertThat(o.takeNext()).isEqualTo(2);
    o.assertOnComplete();
    assertThat(auto.isDisposed()).isTrue();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.subscribe(AutoDispose.observable(lifecycle)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.onSuccess(2);
    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleProvider<Integer> provider = TestUtil.makeProvider(lifecycle);
    source.subscribe(AutoDispose.observable(provider)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifecycle.onNext(1);
    source.onNext(2);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();
    assertThat(o.takeNext()).isEqualTo(2);

    lifecycle.onNext(3);
    source.onNext(3);

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    RecordingObserver<Integer> o = new RecordingObserver<>();
    LifecycleProvider<Integer> provider = TestUtil.makeProvider(lifecycle);
    Observable.just(1)
        .subscribe(AutoDispose.observable(provider)
            .around(o));

    assertThat(o.takeError()).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test
  public void autoDispose_withProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    RecordingObserver<Integer> o = new RecordingObserver<>();
    LifecycleProvider<Integer> provider = TestUtil.makeProvider(lifecycle);
    Observable.just(1)
        .subscribe(AutoDispose.observable(provider)
            .around(o));

    assertThat(o.takeError()).isInstanceOf(LifecycleEndedException.class);
  }

  @Test
  public void verifyCancellation() throws Exception {
    AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    final ObservableEmitter<Integer>[] emitter = new ObservableEmitter[1];
    Observable<Integer> source = Observable.create(e -> {
      e.setCancellable(i::incrementAndGet);
      emitter[0] = e;
    });
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.subscribe(AutoDispose.observable(lifecycle)
        .empty());

    assertThat(i.get()).isEqualTo(0);
    assertThat(lifecycle.hasObservers()).isTrue();

    emitter[0].onNext(1);

    lifecycle.onSuccess(0);
    emitter[0].onNext(2);

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(lifecycle.hasObservers()).isFalse();
  }
}
