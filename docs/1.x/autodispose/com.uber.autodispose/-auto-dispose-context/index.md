[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [AutoDisposeContext](./index.md)

# AutoDisposeContext

`interface AutoDisposeContext`

A context intended for use as `AutoDisposeContext.() -> Unit` function body parameters
where zero-arg [autoDispose](auto-dispose.md) functions can be called. This should be backed by an underlying
[Completable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) or [ScopeProvider](../-scope-provider/index.md).

### Functions

| Name | Summary |
|---|---|
| [autoDispose](auto-dispose.md) | `abstract fun <T> `[`ParallelFlowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`ParallelFlowableSubscribeProxy`](../-parallel-flowable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`<br>`abstract fun <T> `[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`FlowableSubscribeProxy`](../-flowable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`<br>`abstract fun <T> `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`ObservableSubscribeProxy`](../-observable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`<br>`abstract fun <T> `[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`SingleSubscribeProxy`](../-single-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`<br>`abstract fun <T> `[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(): `[`MaybeSubscribeProxy`](../-maybe-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`<br>`abstract fun `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`.autoDispose(): `[`CompletableSubscribeProxy`](../-completable-subscribe-proxy/index.md)<br>Extension that proxies to the normal [autoDispose](auto-dispose.md) extension function. |
