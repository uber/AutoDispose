[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [io.reactivex.parallel.ParallelFlowable](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T> `[`ParallelFlowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): ParallelFlowableSubscribeProxy<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [ParallelFlowable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-dispose.md#com.uber.autodispose.android.lifecycle$autoDispose(io.reactivex.parallel.ParallelFlowable((com.uber.autodispose.android.lifecycle.autoDispose.T)), androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when
subscription will be disposed.

### Parameters

`lifecycleOwner` - The lifecycle owner.

`untilEvent` - Optional lifecycle event when subscription will be disposed.