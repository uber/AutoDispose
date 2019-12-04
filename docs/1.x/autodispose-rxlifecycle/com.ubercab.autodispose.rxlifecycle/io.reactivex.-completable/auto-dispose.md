[autodispose-rxlifecycle](../../index.md) / [com.ubercab.autodispose.rxlifecycle](../index.md) / [io.reactivex.Completable](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <E> `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`.autoDispose(lifecycleProvider: LifecycleProvider<`[`E`](auto-dispose.md#E)`>, event: `[`E`](auto-dispose.md#E)`? = null): CompletableSubscribeProxy`

Extension that proxies to [Completable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) + [AutoDispose.autoDisposable](#) and takes an [event](auto-dispose.md#com.ubercab.autodispose.rxlifecycle$autoDispose(io.reactivex.Completable, com.trello.rxlifecycle2.LifecycleProvider((com.ubercab.autodispose.rxlifecycle.autoDispose.E)), com.ubercab.autodispose.rxlifecycle.autoDispose.E)/event) when
subscription will be disposed.

### Parameters

`lifecycleProvider` - The lifecycle provider from RxLifecycle.

`event` - Optional lifecycle event when subscription will be disposed.