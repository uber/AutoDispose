[autodispose-rxlifecycle3](../../index.md) / [com.ubercab.autodispose.rxlifecycle3](../index.md) / [io.reactivex.Single](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T, E> `[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(lifecycleProvider: LifecycleProvider<`[`E`](auto-dispose.md#E)`>, event: `[`E`](auto-dispose.md#E)`? = null): SingleSubscribeProxy<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [Single.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html) + [AutoDispose.autoDisposable](#) and takes an [event](auto-dispose.md#com.ubercab.autodispose.rxlifecycle3$autoDispose(io.reactivex.Single((com.ubercab.autodispose.rxlifecycle3.autoDispose.T)), com.trello.rxlifecycle3.LifecycleProvider((com.ubercab.autodispose.rxlifecycle3.autoDispose.E)), com.ubercab.autodispose.rxlifecycle3.autoDispose.E)/event) when
subscription will be disposed.

### Parameters

`lifecycleProvider` - The lifecycle provider from RxLifecycle.

`event` - Optional lifecycle event when subscription will be disposed.