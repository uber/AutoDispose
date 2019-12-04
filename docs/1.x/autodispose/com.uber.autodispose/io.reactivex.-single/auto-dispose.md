[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [io.reactivex.Single](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T> `[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(scope: `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`): `[`SingleSubscribeProxy`](../-single-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`@CheckReturnValue inline fun <T> `[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(provider: `[`ScopeProvider`](../-scope-provider/index.md)`): `[`SingleSubscribeProxy`](../-single-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [Single.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html) + [AutoDispose.autoDisposable](../-auto-dispose/auto-disposable.md)

