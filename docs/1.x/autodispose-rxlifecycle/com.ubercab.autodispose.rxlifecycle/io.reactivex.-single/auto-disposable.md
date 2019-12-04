[autodispose-rxlifecycle](../../index.md) / [com.ubercab.autodispose.rxlifecycle](../index.md) / [io.reactivex.Single](index.md) / [autoDisposable](./auto-disposable.md)

# autoDisposable

`@CheckReturnValue inline fun <T, E> `[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)`<`[`T`](auto-disposable.md#T)`>.~~autoDisposable~~(lifecycleProvider: LifecycleProvider<`[`E`](auto-disposable.md#E)`>, event: `[`E`](auto-disposable.md#E)`? = null): SingleSubscribeProxy<`[`T`](auto-disposable.md#T)`>`
**Deprecated:** @kotlin.ReplaceWith

Extension that proxies to [Single.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html) + [AutoDispose.autoDisposable](#) and takes an [event](auto-disposable.md#com.ubercab.autodispose.rxlifecycle$autoDisposable(io.reactivex.Single((com.ubercab.autodispose.rxlifecycle.autoDisposable.T)), com.trello.rxlifecycle2.LifecycleProvider((com.ubercab.autodispose.rxlifecycle.autoDisposable.E)), com.ubercab.autodispose.rxlifecycle.autoDisposable.E)/event) when
subscription will be disposed.

### Parameters

`lifecycleProvider` - The lifecycle provider from RxLifecycle.

`event` - Optional lifecycle event when subscription will be disposed.