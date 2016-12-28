package com.uber.autodispose.clause.subscribe;

import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the around steps that match Maybe's subscribe overloads.
 */
public interface MaybeSubscribeClause {

  <T> MaybeObserver<T> empty();

  <T> MaybeObserver<T> around(Consumer<? super T> onSuccess);

  <T> MaybeObserver<T> around(Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError);

  <T> MaybeObserver<T> around(Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError,
      Action onComplete);

  <T> MaybeObserver<T> around(MaybeObserver<T> observer);

  <T> MaybeObserver<T> around(Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError,
      Action onComplete,
      Consumer<? super Disposable> onSubscribe);
}
