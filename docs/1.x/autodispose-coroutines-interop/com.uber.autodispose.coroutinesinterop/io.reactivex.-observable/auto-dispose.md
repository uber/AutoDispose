[autodispose-coroutines-interop](../../index.md) / [com.uber.autodispose.coroutinesinterop](../index.md) / [io.reactivex.Observable](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`inline fun <T> `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(scope: CoroutineScope): ObservableSubscribeProxy<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to the normal [autoDispose](#) extension function with a [ScopeProvider](#).

