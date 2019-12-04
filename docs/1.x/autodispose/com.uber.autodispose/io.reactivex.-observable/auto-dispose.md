[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [io.reactivex.Observable](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T> `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(scope: `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`): `[`ObservableSubscribeProxy`](../-observable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`
`@CheckReturnValue inline fun <T> `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(provider: `[`ScopeProvider`](../-scope-provider/index.md)`): `[`ObservableSubscribeProxy`](../-observable-subscribe-proxy/index.md)`<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [Observable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html) + [AutoDispose.autoDisposable](../-auto-dispose/auto-disposable.md)

