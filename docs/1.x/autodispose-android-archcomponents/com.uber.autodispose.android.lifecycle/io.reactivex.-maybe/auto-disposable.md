[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [io.reactivex.Maybe](index.md) / [autoDisposable](./auto-disposable.md)

# autoDisposable

`@CheckReturnValue inline fun <T> `[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)`<`[`T`](auto-disposable.md#T)`>.~~autoDisposable~~(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): MaybeSubscribeProxy<`[`T`](auto-disposable.md#T)`>`
**Deprecated:** @kotlin.ReplaceWith

Extension that proxies to [Maybe.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-disposable.md#com.uber.autodispose.android.lifecycle$autoDisposable(io.reactivex.Maybe((com.uber.autodispose.android.lifecycle.autoDisposable.T)), androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when
subscription will be disposed.

### Parameters

`lifecycleOwner` - The lifecycle owner.

`untilEvent` - Optional lifecycle event when subscription will be disposed.