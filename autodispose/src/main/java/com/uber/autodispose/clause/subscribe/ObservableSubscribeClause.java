package com.uber.autodispose.clause.subscribe;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the around steps that match Observable's subscribe overloads.
 */
public interface ObservableSubscribeClause {

  <T> Observer<T> empty();

  <T> Observer<T> around(Consumer<? super T> onNext);

  <T> Observer<T> around(Consumer<? super T> onNext, Consumer<? super Throwable> onError);

  <T> Observer<T> around(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete);

  <T> Observer<T> around(Observer<T> observer);

  <T> Observer<T> around(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete,
      Consumer<? super Disposable> onSubscribe);
}
