[autodispose-rxlifecycle3](../../index.md) / [com.ubercab.autodispose.rxlifecycle3](../index.md) / [io.reactivex.Maybe](index.md) / [autoDisposable](./auto-disposable.md)

# autoDisposable

`@CheckReturnValue inline fun <T, E> `[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)`<`[`T`](auto-disposable.md#T)`>.~~autoDisposable~~(lifecycleProvider: LifecycleProvider<`[`E`](auto-disposable.md#E)`>, event: `[`E`](auto-disposable.md#E)`? = null): MaybeSubscribeProxy<`[`T`](auto-disposable.md#T)`>`
**Deprecated:** @kotlin.ReplaceWith

Extension that proxies to [Maybe.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html) + [AutoDispose.autoDisposable](#) and takes an [event](auto-disposable.md#com.ubercab.autodispose.rxlifecycle3$autoDisposable(io.reactivex.Maybe((com.ubercab.autodispose.rxlifecycle3.autoDisposable.T)), com.trello.rxlifecycle3.LifecycleProvider((com.ubercab.autodispose.rxlifecycle3.autoDisposable.E)), com.ubercab.autodispose.rxlifecycle3.autoDisposable.E)/event) when
subscription will be disposed.

### Parameters

`lifecycleProvider` - The lifecycle provider from RxLifecycle.

`event` - Optional lifecycle event when subscription will be disposed.