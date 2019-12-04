[autodispose-rxlifecycle3](../../index.md) / [com.ubercab.autodispose.rxlifecycle3](../index.md) / [io.reactivex.Flowable](index.md) / [autoDisposable](./auto-disposable.md)

# autoDisposable

`@CheckReturnValue inline fun <T, E> `[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)`<`[`T`](auto-disposable.md#T)`>.~~autoDisposable~~(lifecycleProvider: LifecycleProvider<`[`E`](auto-disposable.md#E)`>, event: `[`E`](auto-disposable.md#E)`? = null): FlowableSubscribeProxy<`[`T`](auto-disposable.md#T)`>`
**Deprecated:** @kotlin.ReplaceWith

Extension that proxies to [Flowable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) + [AutoDispose.autoDisposable](#) and takes an [event](auto-disposable.md#com.ubercab.autodispose.rxlifecycle3$autoDisposable(io.reactivex.Flowable((com.ubercab.autodispose.rxlifecycle3.autoDisposable.T)), com.trello.rxlifecycle3.LifecycleProvider((com.ubercab.autodispose.rxlifecycle3.autoDisposable.E)), com.ubercab.autodispose.rxlifecycle3.autoDisposable.E)/event) when
subscription will be disposed.

### Parameters

`lifecycleProvider` - The lifecycle provider from RxLifecycle.

`event` - Optional lifecycle event when subscription will be disposed.