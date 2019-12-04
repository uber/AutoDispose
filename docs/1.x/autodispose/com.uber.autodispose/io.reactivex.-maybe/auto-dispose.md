[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [io.reactivex.Maybe](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T> `[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(scope: `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`): `[`MaybeSubscribeProxy`](../-maybe-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`@CheckReturnValue inline fun <T> `[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(provider: `[`ScopeProvider`](../-scope-provider/index.md)`): `[`MaybeSubscribeProxy`](../-maybe-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [Maybe.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html) + [AutoDispose.autoDisposable](../-auto-dispose/auto-disposable.md)

