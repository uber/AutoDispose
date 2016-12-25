package com.uber.autodispose;

import com.uber.autodispose.clause.scope.CompletableScopeClause;
import com.uber.autodispose.clause.scope.FlowableScopeClause;
import com.uber.autodispose.clause.scope.MaybeScopeClause;
import com.uber.autodispose.clause.scope.ObservableScopeClause;
import com.uber.autodispose.clause.scope.SingleScopeClause;
import com.uber.autodispose.clause.subscribe.CompletableSubscribeClause;
import com.uber.autodispose.clause.subscribe.FlowableSubscribeClause;
import com.uber.autodispose.clause.subscribe.MaybeSubscribeClause;
import com.uber.autodispose.clause.subscribe.ObservableSubscribeClause;
import com.uber.autodispose.clause.subscribe.SingleSubscribeClause;
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

  private static final FlowableScopeClause FLOWABLE_SCOPE_CLAUSE = new FlowableClauseImpl();
  private static final ObservableScopeClause OBSERVABLE_SCOPE_CLAUSE
      = new ObservableScopeClauseImpl();
  private static final MaybeScopeClause MAYBE_SCOPE_CLAUSE = new MaybeScopeClauseImpl();
  private static final SingleScopeClause SINGLE_SCOPE_CLAUSE = new SingleScopeClauseImpl();
  private static final CompletableScopeClause COMPLETABLE_SCOPE_CLAUSE
      = new CompletableScopeClauseImpl();

  private AutoDispose() {
    throw new InstantiationError();
  }

  public static FlowableScopeClause flowable() {
    return FLOWABLE_SCOPE_CLAUSE;
  }

  public static ObservableScopeClause observable() {
    return OBSERVABLE_SCOPE_CLAUSE;
  }

  public static MaybeScopeClause maybe() {
    return MAYBE_SCOPE_CLAUSE;
  }

  public static SingleScopeClause single() {
    return SINGLE_SCOPE_CLAUSE;
  }

  public static CompletableScopeClause completable() {
    return COMPLETABLE_SCOPE_CLAUSE;
  }

  private static class FlowableClauseImpl implements FlowableScopeClause {
    @Override
    public AutoDisposingSubscriberCreator withScope(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingSubscriberCreator(provider);
    }

    @Override
    public AutoDisposingSubscriberCreator withScope(Maybe<?> lifecycle) {
      return new AutoDisposingSubscriberCreator(lifecycle);
    }
  }

  private static class ObservableScopeClauseImpl implements ObservableScopeClause {
    @Override
    public ObservableSubscribeClause withScope(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingObserverCreator(provider);
    }

    @Override
    public ObservableSubscribeClause withScope(Maybe<?> lifecycle) {
      return new AutoDisposingObserverCreator(lifecycle);
    }
  }

  private static class MaybeScopeClauseImpl implements MaybeScopeClause {
    @Override
    public MaybeSubscribeClause withScope(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingMaybeObserverCreator(provider);
    }

    @Override
    public MaybeSubscribeClause withScope(Maybe<?> lifecycle) {
      return new AutoDisposingMaybeObserverCreator(lifecycle);
    }
  }

  private static class SingleScopeClauseImpl implements SingleScopeClause {
    @Override
    public SingleSubscribeClause withScope(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingSingleObserverCreator(provider);
    }

    @Override
    public SingleSubscribeClause withScope(Maybe<?> lifecycle) {
      return new AutoDisposingSingleObserverCreator(lifecycle);
    }
  }

  private static class CompletableScopeClauseImpl implements CompletableScopeClause {
    @Override
    public CompletableSubscribeClause withScope(LifecycleScopeProvider<?> provider) {
      return new AutoDisposingCompletableObserverCreator(provider);
    }

    @Override
    public CompletableSubscribeClause withScope(Maybe<?> lifecycle) {
      return new AutoDisposingCompletableObserverCreator(lifecycle);
    }
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

    Base(LifecycleScopeProvider<?> provider) {
      this(deferredResolvedLifecycle(checkNotNull(provider, "provider == null")));
    }

    Base(Maybe<?> lifecycle) {
      this.lifecycle = checkNotNull(lifecycle, "lifecycle == null");
    }
  }

  private static class AutoDisposingSubscriberCreator extends Base
      implements FlowableSubscribeClause {
    AutoDisposingSubscriberCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingSubscriberCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    @Override
    public <T> Subscriber<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER,
          AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
          AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override
    public <T> Subscriber<T> around(Consumer<? super T> onNext) {
      checkNotNull(onNext, "onNext == null");
      return around(onNext, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER, AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override
    public <T> Subscriber<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      return around(onNext, onError, AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override
    public <T> Subscriber<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError,
        Action onComplete) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      return around(onNext, onError, onComplete, AutoDisposeUtil.EMPTY_SUBSCRIPTION_CONSUMER);
    }

    @Override
    public <T> Subscriber<T> around(Subscriber<T> subscriber) {
      checkNotNull(subscriber, "subscriber == null");
      return around(subscriber::onNext,
          subscriber::onError,
          subscriber::onComplete,
          subscriber::onSubscribe);
    }

    @Override
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

  private static class AutoDisposingObserverCreator extends Base
      implements ObservableSubscribeClause {
    AutoDisposingObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    @Override
    public <T> Observer<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER);
    }

    @Override
    public <T> Observer<T> around(Consumer<? super T> onNext) {
      checkNotNull(onNext, "onNext == null");
      return around(onNext, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER, AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override
    public <T> Observer<T> around(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      return around(onNext, onError, AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override
    public <T> Observer<T> around(Consumer<? super T> onNext,
        Consumer<? super Throwable> onError,
        Action onComplete) {
      checkNotNull(onNext, "onNext == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      return around(onNext, onError, onComplete, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    @Override
    public <T> Observer<T> around(Observer<T> observer) {
      checkNotNull(observer, "observer == null");
      return around(observer::onNext,
          observer::onError,
          observer::onComplete,
          observer::onSubscribe);
    }

    @Override
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

  private static class AutoDisposingSingleObserverCreator extends Base
      implements SingleSubscribeClause {
    AutoDisposingSingleObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingSingleObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    @Override
    public <T> SingleObserver<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER);
    }

    @Override
    public <T> SingleObserver<T> around(Consumer<? super T> onSuccess) {
      checkNotNull(onSuccess, "onSuccess == null");
      return around(onSuccess, AutoDisposeUtil.DEFAULT_ERROR_CONSUMER);
    }

    @Override
    public <T> SingleObserver<T> around(BiConsumer<? super T, ? super Throwable> biConsumer) {
      checkNotNull(biConsumer, "biConsumer == null");
      return around(v -> biConsumer.accept(v, null), t -> biConsumer.accept(null, t));
    }

    @Override
    public <T> SingleObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      return around(onSuccess, onError, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    @Override
    public <T> SingleObserver<T> around(SingleObserver<T> observer) {
      checkNotNull(observer, "observer == null");
      return around(observer::onSuccess, observer::onError, observer::onSubscribe);
    }

    @Override
    public <T> SingleObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError,
        Consumer<? super Disposable> onSubscribe) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onSubscribe, "onSubscribe == null");
      return new AutoDisposingSingleObserver<>(lifecycle, onSuccess, onError, onSubscribe);
    }
  }

  private static class AutoDisposingMaybeObserverCreator extends Base
      implements MaybeSubscribeClause {
    AutoDisposingMaybeObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingMaybeObserverCreator(Maybe<?> lifecycle) {
      super(lifecycle);
    }

    @Override
    public <T> MaybeObserver<T> empty() {
      return around(AutoDisposeUtil.EMPTY_CONSUMER);
    }

    @Override
    public <T> MaybeObserver<T> around(Consumer<? super T> onSuccess) {
      checkNotNull(onSuccess, "onSuccess == null");
      return around(onSuccess,
          AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
          AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override
    public <T> MaybeObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      return around(onSuccess, onError, AutoDisposeUtil.EMPTY_ACTION);
    }

    @Override
    public <T> MaybeObserver<T> around(Consumer<? super T> onSuccess,
        Consumer<? super Throwable> onError,
        Action onComplete) {
      checkNotNull(onSuccess, "onSuccess == null");
      checkNotNull(onError, "onError == null");
      checkNotNull(onComplete, "onComplete == null");
      return around(onSuccess, onError, onComplete, AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER);
    }

    @Override
    public <T> MaybeObserver<T> around(MaybeObserver<T> observer) {
      checkNotNull(observer, "observer == null");
      return around(observer::onSuccess,
          observer::onError,
          observer::onComplete,
          observer::onSubscribe);
    }

    @Override
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

  private static class AutoDisposingCompletableObserverCreator extends Base
      implements CompletableSubscribeClause {
    AutoDisposingCompletableObserverCreator(LifecycleScopeProvider<?> provider) {
      super(provider);
    }

    AutoDisposingCompletableObserverCreator(Maybe<?> lifecycle) {
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
