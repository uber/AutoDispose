package com.uber.autodispose;

import org.reactivestreams.Subscriber;

import io.reactivex.annotations.NonNull;
import io.reactivex.parallel.ParallelFlowable;

/**
 * Subscribe proxy that matches {@link ParallelFlowable}'s subscribe overloads.
 */
public interface ParallelFlowableSubscribeProxy<T> {

    /**
     * Proxy for {@link ParallelFlowable#subscribe(Subscriber[])}.
     */
    void subscribe(@NonNull Subscriber<? super T>[] subscribers);
}
