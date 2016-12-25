package com.uber.autodispose.clause.subscribe;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Subscribe clause for the around steps that match Flowable's subscribe overloads.
 */
public interface FlowableSubscribeClause {

  <T> Subscriber<T> empty();

  <T> Subscriber<T> around(Consumer<? super T> onNext);

  <T> Subscriber<T> around(Consumer<? super T> onNext, Consumer<? super Throwable> onError);

  <T> Subscriber<T> around(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete);

  <T> Subscriber<T> around(Subscriber<T> subscriber);

  <T> Subscriber<T> around(Consumer<? super T> onNext,
      Consumer<? super Throwable> onError,
      Action onComplete,
      Consumer<? super Subscription> onSubscribe);
}
