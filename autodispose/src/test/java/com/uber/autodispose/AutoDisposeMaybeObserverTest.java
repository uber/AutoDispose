package com.uber.autodispose;

import hu.akarnokd.rxjava2.subjects.MaybeSubject;
import io.reactivex.Maybe;
import io.reactivex.subjects.BehaviorSubject;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.TestUtil.makeProvider;

public class AutoDisposeMaybeObserverTest {

  @Test
  public void autoDispose_withMaybe_normal() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    MaybeSubject<Integer> source = MaybeSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.subscribe(AutoDispose.maybe()
        .withScope(lifecycle)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    // Got the event
    source.onSuccess(1);
    assertThat(o.takeSuccess()).isEqualTo(1);

    // Nothing more, lifecycle disposed too
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    MaybeSubject<Integer> source = MaybeSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.subscribe(AutoDispose.maybe()
        .withScope(lifecycle)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    // Lifecycle ends
    lifecycle.onSuccess(2);
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();

    // Event if upstream emits, no one is listening
    source.onSuccess(2);
    o.assertNoMoreEvents();
  }

  @Test
  public void autoDispose_withProvider_success() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    MaybeSubject<Integer> source = MaybeSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeProvider(lifecycle);
    source.subscribe(AutoDispose.maybe()
        .withScope(provider)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onNext(1);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onSuccess(3);
    o.takeSuccess();

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider_completion() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    MaybeSubject<Integer> source = MaybeSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeProvider(lifecycle);
    source.subscribe(AutoDispose.maybe()
        .withScope(provider)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onNext(1);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    source.onComplete();
    o.assertOnComplete();

    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withProvider_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    MaybeSubject<Integer> source = MaybeSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeProvider(lifecycle);
    source.subscribe(AutoDispose.maybe()
        .withScope(provider)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onNext(1);

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onNext(3);

    // All disposed
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();

    // No one is listening
    source.onSuccess(3);
    o.assertNoMoreEvents();
  }

  @Test
  public void autoDispose_withProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    RecordingObserver<Integer> o = new RecordingObserver<>();
    LifecycleScopeProvider<Integer> provider = makeProvider(lifecycle);
    Maybe.just(1)
        .subscribe(AutoDispose.maybe()
            .withScope(provider)
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
    LifecycleScopeProvider<Integer> provider = makeProvider(lifecycle);
    Maybe.just(1)
        .subscribe(AutoDispose.maybe()
            .withScope(provider)
            .around(o));

    assertThat(o.takeError()).isInstanceOf(LifecycleEndedException.class);
  }

  @Test
  public void verifyCancellation() throws Exception {
    AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    Maybe<Integer> source = Maybe.create(e -> e.setCancellable(i::incrementAndGet));
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.subscribe(AutoDispose.maybe()
        .withScope(lifecycle)
        .empty());

    assertThat(i.get()).isEqualTo(0);
    assertThat(lifecycle.hasObservers()).isTrue();

    lifecycle.onSuccess(0);

    // Verify cancellation was called
    assertThat(i.get()).isEqualTo(1);
    assertThat(lifecycle.hasObservers()).isFalse();
  }
}
