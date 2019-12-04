[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [io.reactivex.Flowable](index.md) / [autoDisposable](./auto-disposable.md)

# autoDisposable

`@CheckReturnValue inline fun <T> `[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)`<`[`T`](auto-disposable.md#T)`>.~~autoDisposable~~(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): FlowableSubscribeProxy<`[`T`](auto-disposable.md#T)`>`
**Deprecated:** @kotlin.ReplaceWith

Extension that proxies to [Flowable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-disposable.md#com.uber.autodispose.android.lifecycle$autoDisposable(io.reactivex.Flowable((com.uber.autodispose.android.lifecycle.autoDisposable.T)), androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when
subscription will be disposed.

### Parameters

`lifecycleOwner` - The lifecycle owner.

`untilEvent` - Optional lifecycle event when subscription will be disposed.