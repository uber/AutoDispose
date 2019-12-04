[autodispose-rxlifecycle](../../index.md) / [com.ubercab.autodispose.rxlifecycle](../index.md) / [io.reactivex.Completable](index.md) / [autoDisposable](./auto-disposable.md)

# autoDisposable

`@CheckReturnValue inline fun <E> `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`.~~autoDisposable~~(lifecycleProvider: LifecycleProvider<`[`E`](auto-disposable.md#E)`>, event: `[`E`](auto-disposable.md#E)`? = null): CompletableSubscribeProxy`
**Deprecated:** @kotlin.ReplaceWith

Extension that proxies to [Completable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) + [AutoDispose.autoDisposable](#) and takes an [event](auto-disposable.md#com.ubercab.autodispose.rxlifecycle$autoDisposable(io.reactivex.Completable, com.trello.rxlifecycle2.LifecycleProvider((com.ubercab.autodispose.rxlifecycle.autoDisposable.E)), com.ubercab.autodispose.rxlifecycle.autoDisposable.E)/event) when
subscription will be disposed.

### Parameters

`lifecycleProvider` - The lifecycle provider from RxLifecycle.

`event` - Optional lifecycle event when subscription will be disposed.