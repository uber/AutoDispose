package com.uber.autodispose.clause.subscribe;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the around steps that match Single's subscribe overloads.
 */
public interface SingleSubscribeClause {

  <T> SingleObserver<T> empty();

  <T> SingleObserver<T> around(Consumer<? super T> onSuccess);

  <T> SingleObserver<T> around(BiConsumer<? super T, ? super Throwable> biConsumer);

  <T> SingleObserver<T> around(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError);

  <T> SingleObserver<T> around(SingleObserver<T> observer);

  <T> SingleObserver<T> around(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError,
      Consumer<? super Disposable> onSubscribe);
}
