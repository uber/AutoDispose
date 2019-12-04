[autodispose-rxlifecycle](../../index.md) / [com.ubercab.autodispose.rxlifecycle](../index.md) / [io.reactivex.Flowable](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T, E> `[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(lifecycleProvider: LifecycleProvider<`[`E`](auto-dispose.md#E)`>, event: `[`E`](auto-dispose.md#E)`? = null): FlowableSubscribeProxy<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [Flowable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) + [AutoDispose.autoDisposable](#) and takes an [event](auto-dispose.md#com.ubercab.autodispose.rxlifecycle$autoDispose(io.reactivex.Flowable((com.ubercab.autodispose.rxlifecycle.autoDispose.T)), com.trello.rxlifecycle2.LifecycleProvider((com.ubercab.autodispose.rxlifecycle.autoDispose.E)), com.ubercab.autodispose.rxlifecycle.autoDispose.E)/event) when
subscription will be disposed.

### Parameters

`lifecycleProvider` - The lifecycle provider from RxLifecycle.

`event` - Optional lifecycle event when subscription will be disposed.