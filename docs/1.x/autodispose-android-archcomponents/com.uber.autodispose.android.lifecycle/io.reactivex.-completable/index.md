[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [io.reactivex.Completable](./index.md)

### Extensions for io.reactivex.Completable

| Name | Summary |
|---|---|
| [autoDisposable](auto-disposable.md) | `fun `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`.~~autoDisposable~~(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): CompletableSubscribeProxy`<br>Extension that proxies to [Completable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-disposable.md#com.uber.autodispose.android.lifecycle$autoDisposable(io.reactivex.Completable, androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when subscription will be disposed. |
| [autoDispose](auto-dispose.md) | `fun `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`.autoDispose(lifecycleOwner: LifecycleOwner, untilEvent: Event? = null): CompletableSubscribeProxy`<br>Extension that proxies to [Completable.as](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) + [AutoDispose.autoDisposable](#) and takes an [untilEvent](auto-dispose.md#com.uber.autodispose.android.lifecycle$autoDispose(io.reactivex.Completable, androidx.lifecycle.LifecycleOwner, androidx.lifecycle.Lifecycle.Event)/untilEvent) when subscription will be disposed. |
