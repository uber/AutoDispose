[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [io.reactivex.parallel.ParallelFlowable](index.md) / [autoDisposable](./auto-disposable.md)

# autoDisposable

`@CheckReturnValue inline fun <T> `[`ParallelFlowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html)`<`[`T`](auto-disposable.md#T)`>.~~autoDisposable~~(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): ParallelFlowableSubscribeProxy<`[`T`](auto-disposable.md#T)`>`
**Deprecated:** @kotlin.ReplaceWith

Extension that proxies to [ParallelFlowable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-disposable.md#com.uber.autodispose.android.lifecycle$autoDisposable(io.reactivex.parallel.ParallelFlowable((com.uber.autodispose.android.lifecycle.autoDisposable.T)), androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when
subscription will be disposed.

### Parameters

`lifecycleOwner` - The lifecycle owner.

`untilEvent` - Optional lifecycle event when subscription will be disposed.