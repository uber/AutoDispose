[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [io.reactivex.Maybe](index.md) / [autoDispose](./auto-dispose.md)

# autoDispose

`@CheckReturnValue inline fun <T> `[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): MaybeSubscribeProxy<`[`T`](auto-dispose.md#T)`>`

Extension that proxies to [Maybe.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-dispose.md#com.uber.autodispose.android.lifecycle$autoDispose(io.reactivex.Maybe((com.uber.autodispose.android.lifecycle.autoDispose.T)), androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when
subscription will be disposed.

### Parameters

`lifecycleOwner` - The lifecycle owner.

`untilEvent` - Optional lifecycle event when subscription will be disposed.