[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [io.reactivex.Flowable](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T> `[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(scope: `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`): `[`FlowableSubscribeProxy`](../-flowable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`@CheckReturnValue inline fun <T> `[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(provider: `[`ScopeProvider`](../-scope-provider/index.md)`): `[`FlowableSubscribeProxy`](../-flowable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [Flowable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) + [AutoDispose.autoDisposable](../-auto-dispose/auto-disposable.md)

