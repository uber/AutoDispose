package com.uber.autodispose.clause.subscribe;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the around steps that match Flowable's subscribe overloads.
 */
public interface CompletableSubscribeClause {

  CompletableObserver empty();

  CompletableObserver around(Action action);

  CompletableObserver around(Action action, Consumer<? super Throwable> onError);

  CompletableObserver around(CompletableObserver observer);

  CompletableObserver around(Action action, Consumer<? super Throwable> onError,
      Consumer<? super Disposable> onSubscribe);
}
