[autodispose-coroutines-interop](../../index.md) / [com.uber.autodispose.coroutinesinterop](../index.md) / [kotlinx.coroutines.CoroutineScope](index.md) / [asCompletable](./as-completable.md)

# asCompletable

`fun CoroutineScope.asCompletable(): `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)

**Return**
a [Completable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representation of this [CoroutineScope](#). This will complete when [this](as-completable/-this-.md)
    coroutine scope completes. Note that the returned [Completable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) is deferred.

