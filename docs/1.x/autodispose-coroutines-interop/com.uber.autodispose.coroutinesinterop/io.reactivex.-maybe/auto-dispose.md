[autodispose-coroutines-interop](../../index.md) / [com.uber.autodispose.coroutinesinterop](../index.md) / [io.reactivex.Maybe](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`inline fun <T> `[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(scope: CoroutineScope): MaybeSubscribeProxy<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to the normal [autoDispose](#) extension function with a [ScopeProvider](#).

