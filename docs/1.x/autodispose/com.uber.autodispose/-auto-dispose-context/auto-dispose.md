[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [AutoDisposeContext](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`abstract fun <T> `[`ParallelFlowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`ParallelFlowableSubscribeProxy`](../-parallel-flowable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`abstract fun <T> `[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`FlowableSubscribeProxy`](../-flowable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`abstract fun <T> `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`ObservableSubscribeProxy`](../-observable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`abstract fun <T> `[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`SingleSubscribeProxy`](../-single-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`abstract fun <T> `[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`MaybeSubscribeProxy`](../-maybe-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`abstract fun `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`.autoDispose(): `[`CompletableSubscribeProxy`](../-completable-subscribe-proxy/index.md)

Extension that proxies to the normal [autoDispose](./auto-dispose.md) extension function.

