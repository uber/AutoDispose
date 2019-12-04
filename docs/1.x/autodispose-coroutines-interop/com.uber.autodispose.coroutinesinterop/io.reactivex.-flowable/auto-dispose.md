[autodispose-coroutines-interop](../../index.md) / [com.uber.autodispose.coroutinesinterop](../index.md) / [io.reactivex.Flowable](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`inline fun <T> `[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(scope: CoroutineScope): FlowableSubscribeProxy<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to the normal [autoDispose](#) extension function with a [ScopeProvider](#).

