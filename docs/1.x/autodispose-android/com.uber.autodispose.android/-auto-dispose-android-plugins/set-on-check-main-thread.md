[autodispose-android](../../index.md) / [com.uber.autodispose.android](../index.md) / [AutoDisposeAndroidPlugins](index.md) / [setOnCheckMainThread](./set-on-check-main-thread.md)

# setOnCheckMainThread

`static fun setOnCheckMainThread(@Nullable mainThreadChecker: `[`BooleanSupplier`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html)`?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Sets the preferred main thread checker. If not `null`, the `mainThreadChecker` will be preferred in all main thread checks in ``[`#onCheckMainThread(BooleanSupplier)`](on-check-main-thread.md) calls. This can be useful for JVM testing environments, where standard Android Looper APIs cannot be stubbed and thus should be overridden with a custom check.

This is a reset-able API, which means you can pass `null` as the parameter value to reset it. Alternatively, you can call ``[`#reset()`](reset.md).

### Parameters

`mainThreadChecker` - [BooleanSupplier](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html)?: a ``[`BooleanSupplier`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html) to call to check if current execution is on the main thread. Should return `true` if it is on the main thread or `false` if not.`static fun setOnCheckMainThread(@Nullable mainThreadChecker: `[`BooleanSupplier`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html)`?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Sets the preferred main thread checker. If not `null`, the `mainThreadChecker` will be preferred in all main thread checks in ``[`#onCheckMainThread(BooleanSupplier)`](on-check-main-thread.md) calls. This can be useful for JVM testing environments, where standard Android Looper APIs cannot be stubbed and thus should be overridden with a custom check.

This is a reset-able API, which means you can pass `null` as the parameter value to reset it. Alternatively, you can call ``[`#reset()`](reset.md).

### Parameters

`mainThreadChecker` - [BooleanSupplier](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html)?: a ``[`BooleanSupplier`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html) to call to check if current execution is on the main thread. Should return `true` if it is on the main thread or `false` if not.