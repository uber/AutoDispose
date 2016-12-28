package com.uber.autodispose;

import hu.akarnokd.rxjava2.subjects.CompletableSubject;
import hu.akarnokd.rxjava2.subjects.MaybeSubject;
import io.reactivex.Completable;
import io.reactivex.subjects.BehaviorSubject;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.uber.autodispose.TestUtil.makeLifecycleProvider;
import static com.uber.autodispose.TestUtil.makeProvider;

public class AutoDisposeCompletableObserverTest {

  @Test
  public void autoDispose_withMaybe_normal() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    CompletableSubject source = CompletableSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.subscribe(AutoDispose.completable()
        .withScope(lifecycle)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(lifecycle.hasObservers()).isTrue();

    // Got the event
    source.onComplete();
    o.assertOnComplete();

    // Nothing more, lifecycle disposed too
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
    assertThat(lifecycle.hasObservers()).isFalse();
  }

  @Test
  public void autoDispose_withMaybe_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    CompletableSubject source = CompletableSubject.create();
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.subscribe(AutoDispose.completable()
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
    source.onComplete();
    o.assertNoMoreEvents();
  }

  @Test
  public void autoDispose_withProvider_completion() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    CompletableSubject source = CompletableSubject.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.subscribe(AutoDispose.completable()
        .withScope(provider)
        .around(o));
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
    RecordingObserver<Integer> o = new RecordingObserver<>();
    CompletableSubject source = CompletableSubject.create();
    MaybeSubject<Integer> scope = MaybeSubject.create();
    ScopeProvider provider = makeProvider(scope);
    source.subscribe(AutoDispose.completable()
        .withScope(provider)
        .around(o));
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();
    assertThat(scope.hasObservers()).isTrue();

    scope.onSuccess(1);

    // All disposed
    assertThat(source.hasObservers()).isFalse();
    assertThat(scope.hasObservers()).isFalse();

    // No one is listening
    source.onComplete();
    o.assertNoMoreEvents();
  }

  @Test
  public void autoDispose_withLifecycleProvider_completion() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    CompletableSubject source = CompletableSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    source.subscribe(AutoDispose.completable()
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
  public void autoDispose_withLifecycleProvider_interrupted() {
    RecordingObserver<Integer> o = new RecordingObserver<>();
    CompletableSubject source = CompletableSubject.create();
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    source.subscribe(AutoDispose.completable()
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
    source.onComplete();
    o.assertNoMoreEvents();
  }

  @Test
  public void autoDispose_withLifecycleProvider_withoutStartingLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.create();
    RecordingObserver<Integer> o = new RecordingObserver<>();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    Completable.complete()
        .subscribe(AutoDispose.completable()
            .withScope(provider)
            .around(o));

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleNotStartedException.class);
  }

  @Test
  public void autoDispose_withLifecycleProvider_afterLifecycle_shouldFail() {
    BehaviorSubject<Integer> lifecycle = BehaviorSubject.createDefault(0);
    lifecycle.onNext(1);
    lifecycle.onNext(2);
    lifecycle.onNext(3);
    RecordingObserver<Integer> o = new RecordingObserver<>();
    LifecycleScopeProvider<Integer> provider = makeLifecycleProvider(lifecycle);
    Completable.complete()
        .subscribe(AutoDispose.completable()
            .withScope(provider)
            .around(o));

    o.takeSubscribe();
    assertThat(o.takeError()).isInstanceOf(LifecycleEndedException.class);
  }

  @Test
  public void verifyCancellation() throws Exception {
    AtomicInteger i = new AtomicInteger();
    //noinspection unchecked because Java
    Completable source = Completable.create(e -> e.setCancellable(i::incrementAndGet));
    MaybeSubject<Integer> lifecycle = MaybeSubject.create();
    source.subscribe(AutoDispose.completable()
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
