[autodispose-rxlifecycle](../../index.md) / [com.ubercab.autodispose.rxlifecycle](../index.md) / [io.reactivex.parallel.ParallelFlowable](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T, E> `[`ParallelFlowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(lifecycleProvider: LifecycleProvider<`[`E`](auto-dispose.md#E)`>, event: `[`E`](auto-dispose.md#E)`? = null): ParallelFlowableSubscribeProxy<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [ParallelFlowable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html) + [AutoDispose.autoDisposable](#) and takes an [event](auto-dispose.md#com.ubercab.autodispose.rxlifecycle$autoDispose(io.reactivex.parallel.ParallelFlowable((com.ubercab.autodispose.rxlifecycle.autoDispose.T)), com.trello.rxlifecycle2.LifecycleProvider((com.ubercab.autodispose.rxlifecycle.autoDispose.E)), com.ubercab.autodispose.rxlifecycle.autoDispose.E)/event) when
subscription will be disposed.

### Parameters

`lifecycleProvider` - The lifecycle provider from RxLifecycle.

`event` - Optional lifecycle event when subscription will be disposed.