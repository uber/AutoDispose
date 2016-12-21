@file:Suppress("NOTHING_TO_INLINE")

package com.uber.autodispose.kotlin

import com.uber.autodispose.AutoDispose
import com.uber.autodispose.LifecycleProvider
import com.uber.autodispose.internal.AutoDisposeUtil
import com.uber.autodispose.internal.AutoDisposeUtil.emptyConsumer
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.MaybeObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.BiConsumer
import io.reactivex.functions.Consumer
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

@CheckReturnValue
inline fun <T> Observer<T>.disposeOn(provider: LifecycleProvider<Any>): Observer<T>
    = AutoDispose.observable(provider).around(this)

@CheckReturnValue
inline fun <T> SingleObserver<T>.disposeOn(provider: LifecycleProvider<Any>): SingleObserver<T>
    = AutoDispose.single(provider).around(this)

@CheckReturnValue
inline fun <T> MaybeObserver<T>.disposeOn(provider: LifecycleProvider<Any>): MaybeObserver<T>
    = AutoDispose.maybe(provider).around(this)

@CheckReturnValue
inline fun CompletableObserver.disposeOn(provider: LifecycleProvider<Any>): CompletableObserver
    = AutoDispose.completable(provider).around(this)

@CheckReturnValue
inline fun <T> Subscriber<T>.cancelOn(provider: LifecycleProvider<Any>): Subscriber<T>
    = AutoDispose.flowable(provider).around(this)

@CheckReturnValue
inline fun <T> Observer<T>.disposeOn(lifecycle: Maybe<Any>): Observer<T>
    = AutoDispose.observable(lifecycle).around(this)

@CheckReturnValue
inline fun <T> SingleObserver<T>.disposeOn(lifecycle: Maybe<Any>): SingleObserver<T>
    = AutoDispose.single(lifecycle).around(this)

@CheckReturnValue
inline fun <T> MaybeObserver<T>.disposeOn(lifecycle: Maybe<Any>): MaybeObserver<T>
    = AutoDispose.maybe(lifecycle).around(this)

@CheckReturnValue
inline fun CompletableObserver.disposeOn(lifecycle: Maybe<Any>): CompletableObserver
    = AutoDispose.completable(lifecycle).around(this)

@CheckReturnValue
inline fun <T> Subscriber<T>.cancelOn(lifecycle: Maybe<Any>): Subscriber<T>
    = AutoDispose.flowable(lifecycle).around(this)

inline fun <T> Flowable<T>.subscribeAutoDispose(provider: LifecycleProvider<Any>,
    onNext: Consumer<in T> = emptyConsumer(),
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onComplete: Action = AutoDisposeUtil.EMPTY_ACTION,
    onSubscribe: Consumer<in Subscription> = AutoDisposeUtil.EMPTY_SUBSCRIPTION_CONSUMER)
    = subscribe(AutoDispose.flowable(provider).around(onNext, onError, onComplete, onSubscribe))

inline fun <T> Observable<T>.subscribeAutoDispose(provider: LifecycleProvider<Any>,
    onNext: Consumer<in T> = emptyConsumer(),
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onComplete: Action = AutoDisposeUtil.EMPTY_ACTION,
    onSubscribe: Consumer<in Disposable> = AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER)
    = subscribe(AutoDispose.observable(provider).around(onNext, onError, onComplete, onSubscribe))

inline fun <T> Maybe<T>.subscribeAutoDispose(provider: LifecycleProvider<Any>,
    onSuccess: Consumer<in T> = emptyConsumer(),
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onComplete: Action = AutoDisposeUtil.EMPTY_ACTION,
    onSubscribe: Consumer<in Disposable> = AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER)
    = subscribe(AutoDispose.maybe(provider).around(onSuccess, onError, onComplete, onSubscribe))

inline fun <T> Single<T>.subscribeAutoDispose(provider: LifecycleProvider<Any>,
    biConsumer: BiConsumer<in T, in Throwable>)
    = subscribe(AutoDispose.single(provider).around(biConsumer))

inline fun <T> Single<T>.subscribeAutoDispose(provider: LifecycleProvider<Any>,
    onSuccess: Consumer<in T> = emptyConsumer(),
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onSubscribe: Consumer<in Disposable> = AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER)
    = subscribe(AutoDispose.single(provider).around(onSuccess, onError, onSubscribe))

inline fun Completable.subscribeAutoDispose(provider: LifecycleProvider<Any>,
    onComplete: Action = AutoDisposeUtil.EMPTY_ACTION,
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onSubscribe: Consumer<in Disposable> = AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER)
    = subscribe(AutoDispose.completable(provider).around(onComplete, onError, onSubscribe))

inline fun <T> Flowable<T>.subscribeAutoDispose(lifecycle: Maybe<Any>,
    onNext: Consumer<in T> = emptyConsumer(),
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onComplete: Action = AutoDisposeUtil.EMPTY_ACTION,
    onSubscribe: Consumer<in Subscription> = AutoDisposeUtil.EMPTY_SUBSCRIPTION_CONSUMER)
    = subscribe(AutoDispose.flowable(lifecycle).around(onNext, onError, onComplete, onSubscribe))

inline fun <T> Observable<T>.subscribeAutoDispose(lifecycle: Maybe<Any>,
    onNext: Consumer<in T> = emptyConsumer(),
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onComplete: Action = AutoDisposeUtil.EMPTY_ACTION,
    onSubscribe: Consumer<in Disposable> = AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER)
    = subscribe(AutoDispose.observable(lifecycle).around(onNext, onError, onComplete, onSubscribe))

inline fun <T> Maybe<T>.subscribeAutoDispose(lifecycle: Maybe<Any>,
    onSuccess: Consumer<in T> = emptyConsumer(),
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onComplete: Action = AutoDisposeUtil.EMPTY_ACTION,
    onSubscribe: Consumer<in Disposable> = AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER)
    = subscribe(AutoDispose.maybe(lifecycle).around(onSuccess, onError, onComplete, onSubscribe))

inline fun <T> Single<T>.subscribeAutoDispose(lifecycle: Maybe<Any>,
    biConsumer: BiConsumer<in T, in Throwable>)
    = subscribe(AutoDispose.single(lifecycle).around(biConsumer))

inline fun <T> Single<T>.subscribeAutoDispose(lifecycle: Maybe<Any>,
    onSuccess: Consumer<in T> = emptyConsumer(),
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onSubscribe: Consumer<in Disposable> = AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER)
    = subscribe(AutoDispose.single(lifecycle).around(onSuccess, onError, onSubscribe))

inline fun Completable.subscribeAutoDispose(lifecycle: Maybe<Any>,
    onComplete: Action = AutoDisposeUtil.EMPTY_ACTION,
    onError: Consumer<in Throwable> = AutoDisposeUtil.DEFAULT_ERROR_CONSUMER,
    onSubscribe: Consumer<in Disposable> = AutoDisposeUtil.EMPTY_DISPOSABLE_CONSUMER)
    = subscribe(AutoDispose.completable(lifecycle).around(onComplete, onError, onSubscribe))
