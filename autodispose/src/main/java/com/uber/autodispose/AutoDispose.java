package com.uber.autodispose;

import com.uber.autodispose.internal.AutoDisposeUtil;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static com.uber.autodispose.internal.AutoDisposeUtil.checkNotNull;

public final class AutoDispose {

  private static final Function<Object, LifecycleEndEvent> TRANSFORM_TO_END =
      o -> LifecycleEndEvent.INSTANCE;

  private AutoDispose() {
    throw new InstantiationError();
  }

  public static AutoDisposingSubscriberCreator flowable(LifecycleScopeProvider<?> provider) {
    return new AutoDisposingSubscriberCreator(provider);
  }

  public static AutoDisposingSubscriberCreator flowable(Maybe<?> lifecycle) {
    return new AutoDisposingSubscriberCreator(lifecycle);
  }

  public static AutoDisposingObserverCreator observable(LifecycleScopeProvider<?> provider) {
    return new AutoDisposingObserverCreator(provider);
  }

  public static AutoDisposingObserverCreator observable(Maybe<?> lifecycle) {
    return new AutoDisposingObserverCreator(lifecycle);
  }

  public static AutoDisposingSingleObserverCreator single(
      LifecycleScopeProvider<?> provider) {
    return new AutoDisposingSingleObserverCreator(provider);
  }

  public static AutoDisposingSingleObserverCreator single(Maybe<?> lifecycle) {
    return new AutoDisposingSingleObserverCreator(lifecycle);
  }

  public static AutoDisposingMaybeObserverCreator maybe(
      LifecycleScopeProvider<?> provider) {
    return new AutoDisposingMaybeObserverCreator(provider);
  }

  public static AutoDisposingMaybeObserverCreator maybe(Maybe<?> lifecycle) {
    return new AutoDisposingMaybeObserverCreator(lifecycle);
  }

  public static AutoDisposingCompletableObserverCreator completable(
      LifecycleScopeProvider<?> provider) {
    return new AutoDisposingCompletableObserverCreator(provider);
  }

  public static AutoDisposingCompletableObserverCreator completable(Maybe<?> lifecycle) {
    return new AutoDisposingCompletableObserverCreator(lifecycle);
  }

  private static <E> Maybe<LifecycleEndEvent> deferredResolvedLifecycle(
      LifecycleScopeProvider<E> provider) {
    return Maybe.defer(() -> {
      E lastEvent = provider.peekLifecycle();
      if (lastEvent == null) {
        throw new LifecycleNotStartedException();
      }
      E endEvent = provider.correspondingEvents()
          .apply(lastEvent);
      return mapEvents(provider.lifecycle(), endEvent);
    });
  }

  private static <E> Maybe<LifecycleEndEvent> mapEvents(Observable<E> lifecycle, E endEvent) {
    return lifecycle.skip(1)
        .map(e -> e.equals(endEvent))
        .filter(b -> b)
        .map(TRANSFORM_TO_END)
        .firstElement();
  }

  private enum LifecycleEndEvent {
    INSTANCE
  }

  private static class Base {
    protected final Maybe<?> lifecycle;

    protected Base(LifecycleScopeProvider<?> provider) {
      this(deferredResolvedLifecycle(checkNotNull(provider, "provider == null")));
    }

    protected Base(Maybe<?> lifecycle) {
      this.lifecycle = checkNotNull(lifecycle, "lifecycle == null");
    }
  }

  public static class AutoDisposingSubscriberCreator extends Base {
    private AutoDisposingSubscriberCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    private AutoDisposingSubscriberCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    public <T> Subscriber<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER,
          AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
          AutoDisposeUtil.EMPTY_ACTION);
    }

    public <T> Subscriber<T> around(Consumer<? super T> onNext) {
      checkNotNull(onNext, "onNext == null");
      return around(onNext, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER, AutoDisposeUtil.EMPTY_ACTION);
    }

    public <T> Subscriber<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      return around(onNext, onError, AutoDisposeUtil.EMPTY_ACTION);
    }

    public <T> Subscriber<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError,
        Action onComplete) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      return around(onNext, onError, onComplete, AutoDisposeUtil.EMPTY_SUBSCRIPTION_CONSUMER);
    }

    public <T> Subscriber<T> around(Subscriber<T> subscriber) {
      checkNotNull(subscriber, "subscriber == null");
      return around(subscriber::onNext,
          subscriber::onError,
          subscriber::onComplete,
          subscriber::onSubscribe);
    }

    public <T> Subscriber<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError,
        Action onComplete,
        Consumer<? super Subscription> onSubscribe) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingSubscriber<>(lifecycle, onNext, onError, onComplete, onSubscribe);
    }
  }

  public static class AutoDisposingObserverCreator extends Base {
    private AutoDisposingObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    private AutoDisposingObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    public <T> Observer<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER);
    }

    public <T> Observer<T> around(Consumer<? super T> onNext) {
      checkNotNull(onNext, "onNext == null");
      return around(onNext, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER, AutoDisposeUtil.EMPTY_ACTION);
    }

    public <T> Observer<T> around(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      return around(onNext, onError, AutoDisposeUtil.EMPTY_ACTION);
    }

    public <T> Observer<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError,
        Action onComplete) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      return around(onNext, onError, onComplete, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    public <T> Observer<T> around(Observer<T> observer) {
      checkNotNull(observer, "observer == null");
      return around(observer::onNext,
          observer::onError,
          observer::onComplete,
          observer::onSubscribe);
    }

    public <T> Observer<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError,
        Action onComplete,
        Consumer<? super Disposable> onSubscribe) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingObserver<>(lifecycle, onNext, onError, onComplete, onSubscribe);
    }
  }

  public static class AutoDisposingSingleObserverCreator extends Base {
    private AutoDisposingSingleObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    private AutoDisposingSingleObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    public <T> SingleObserver<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER);
    }

    public <T> SingleObserver<T> around(Consumer<? super T> onSuccess) {
      checkNotNull(onSuccess, "onSuccess == null");
      return around(onSuccess, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER);
    }

    public <T> SingleObserver<T> around(BiConsumer<? super T, ? super Throwable> biConsumer) {
      checkNotNull(biConsumer, "biConsumer == null");
      return around(v -> biConsumer.accept(v, null), t -> biConsumer.accept(null, t));
    }

    public <T> SingleObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      return around(onSuccess, onError, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    public <T> SingleObserver<T> around(SingleObserver<T> observer) {
      checkNotNull(observer, "observer == null");
      return around(observer::onSuccess, observer::onError, observer::onSubscribe);
    }

    public <T> SingleObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError,
        Consumer<? super Disposable> onSubscribe) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingSingleObserver<>(lifecycle, onSuccess, onError, onSubscribe);
    }
  }

  public static class AutoDisposingMaybeObserverCreator extends Base {
    private AutoDisposingMaybeObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    private AutoDisposingMaybeObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    public <T> MaybeObserver<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER);
    }

    public <T> MaybeObserver<T> around(Consumer<? super T> onSuccess) {
      checkNotNull(onSuccess, "onSuccess == null");
      return around(onSuccess,
          AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
          AutoDisposeUtil.EMPTY_ACTION);
    }

    public <T> MaybeObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      return around(onSuccess, onError, AutoDisposeUtil.EMPTY_ACTION);
    }

    public <T> MaybeObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError,
        Action onComplete) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      return around(onSuccess, onError, onComplete, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    public <T> MaybeObserver<T> around(MaybeObserver<T> observer) {
      checkNotNull(observer, "observer == null");
      return around(observer::onSuccess,
          observer::onError,
          observer::onComplete,
          observer::onSubscribe);
    }

    public <T> MaybeObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError,
        Action onComplete,
        Consumer<? super Disposable> onSubscribe) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingMaybeObserver<>(lifecycle,
          onSuccess,
          onError,
          onComplete,
          onSubscribe);
    }
  }

  public static class AutoDisposingCompletableObserverCreator extends Base {
    private AutoDisposingCompletableObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    private AutoDisposingCompletableObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    public CompletableObserver empty() {
      return around(AutoDisposeUtil.EMPTY_ACTION);
    }

    public CompletableObserver around(Action action) {
      checkNotNull(action, "action == null");
      return around(action, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER);
    }

    public CompletableObserver around(Action action, Consumer<? super Throwable> onError) {
      checkNotNull(action, "action == null");
      checkNotNull(onError, "onError == null");
      return around(action, onError, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    public CompletableObserver around(CompletableObserver observer) {
      checkNotNull(observer, "observer == null");
      return around(observer::onComplete, observer::onError, observer::onSubscribe);
    }

    public CompletableObserver around(Action action,
        Consumer<? super Throwable> onError,
        Consumer<? super Disposable> onSubscribe) {
      checkNotNull(action, "action == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingCompletableObserver(lifecycle, action, onError, onSubscribe);
    }
  }
}
