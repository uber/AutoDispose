package com.ubercab.autodispose.rxlifecycleinterop;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.test.RecordingObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class RXLifeCycleInteropTest {

  private static final RecordingObserver.Logger LOGGER = new RecordingObserver.Logger() {
    @Override public void log(String message) {
      System.out.println(RXLifeCycleInteropTest.class.getSimpleName() + ": " + message);
    }
  };

  private LifeCycleProviderImpl lifeCycleProvider = new LifeCycleProviderImpl();

  @Before
  public void setup() {
    lifeCycleProvider.onCreate();
  }

  @Test
  public void bindLifecycle_normalTermination_completeTheStream() {
    TestObserver<Integer> o = new TestObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    Disposable d = source.to(AutoDispose.with(
        RXLifeCycleInterop.bindLifecycle(lifeCycleProvider)).<Integer>forObservable())
        .subscribeWith(o);
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);
    source.onComplete();
    o.assertValues(1, 2);
    o.assertComplete();
    assertThat(d.isDisposed()).isFalse();   // Because it completed normally, was not disposed.
    assertThat(source.hasObservers()).isFalse();
  }

  @Test
  public void bindLifecycle_normalTermination_unsubscribe() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    source.to(AutoDispose.with(
        RXLifeCycleInterop.bindLifecycle(lifeCycleProvider)).<Integer>forObservable())
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifeCycleProvider.onDestroy();
    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
  }

  @Test
  public void bindLifecycle_errorTermination_unsubscribe() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    lifeCycleProvider.onDestroy();
    source
        .to(AutoDispose.with(
            RXLifeCycleInterop.bindLifecycle(lifeCycleProvider)).<Integer>forObservable())
        .subscribe(o);

    o.takeSubscribe();

    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(
        source.hasObservers()).isFalse(); // Because RXLifeCycle treats lifecycle ended exception as terminal event.
  }

  @Test
  public void bindUntilEvent_normalTermination_completeTheStream() {
    TestObserver<Integer> o = new TestObserver<>();
    PublishSubject<Integer> source = PublishSubject.create();
    Disposable d = source.to(AutoDispose.with(RXLifeCycleInterop.bindUntilEvent(lifeCycleProvider,
        LifeCycleProviderImpl.Event.DESTROY)).<Integer>forObservable())
        .subscribeWith(o);
    o.assertSubscribed();

    assertThat(source.hasObservers()).isTrue();

    source.onNext(1);
    o.assertValue(1);

    source.onNext(2);
    source.onComplete();
    o.assertValues(1, 2);
    o.assertComplete();
    assertThat(d.isDisposed()).isFalse();   // Because it completed normally, was not disposed.
    assertThat(source.hasObservers()).isFalse();
  }

  @Test
  public void bindUntilEvent_interruptedTermination_unsubscribe() {
    RecordingObserver<Integer> o = new RecordingObserver<>(LOGGER);
    PublishSubject<Integer> source = PublishSubject.create();
    source.to(AutoDispose.with(RXLifeCycleInterop.bindUntilEvent(lifeCycleProvider,
        LifeCycleProviderImpl.Event.DESTROY)).<Integer>forObservable())
        .subscribe(o);
    o.takeSubscribe();

    assertThat(source.hasObservers()).isTrue();

    source.onNext(1);
    assertThat(o.takeNext()).isEqualTo(1);

    lifeCycleProvider.onDestroy();
    source.onNext(2);
    o.assertNoMoreEvents();
    assertThat(source.hasObservers()).isFalse();
  }
}
