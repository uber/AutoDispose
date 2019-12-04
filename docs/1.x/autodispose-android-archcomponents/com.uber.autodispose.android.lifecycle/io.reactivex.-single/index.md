[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [io.reactivex.Single](./index.md)

### Extensions for io.reactivex.Single

| Name | Summary |
|---|---|
| [autoDisposable](auto-disposable.md) | `fun <T> `[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)`<`[`T`](auto-disposable.md#T)`>.~~autoDisposable~~(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): SingleSubscribeProxy<`[`T`](auto-disposable.md#T)`>`<br>Extension that proxies to [Single.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-disposable.md#com.uber.autodispose.android.lifecycle$autoDisposable(io.reactivex.Single((com.uber.autodispose.android.lifecycle.autoDisposable.T)), androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when subscription will be disposed. |
| [autoDispose](auto-dispose.md) | `fun <T> `[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)`<`[`T`](auto-dispose.md#T)`>.autoDispose(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): SingleSubscribeProxy<`[`T`](auto-dispose.md#T)`>`<br>Extension that proxies to [Single.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-dispose.md#com.uber.autodispose.android.lifecycle$autoDispose(io.reactivex.Single((com.uber.autodispose.android.lifecycle.autoDispose.T)), androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when subscription will be disposed. |