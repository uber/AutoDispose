[autodispose-android](../../index.md) / [com.uber.autodispose.android](../index.md) / [AutoDisposeAndroidPlugins](index.md) / [onCheckMainThread](./on-check-main-thread.md)

# onCheckMainThread

`static fun onCheckMainThread(defaultChecker: `[`BooleanSupplier`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html)`!): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

Returns `true` if called on the main thread, `false` if not. This will prefer a set checker via ``[`#setOnCheckMainThread(BooleanSupplier)`](set-on-check-main-thread.md) if one is present, otherwise it will use `defaultChecker`.

### Parameters

`defaultChecker` - [BooleanSupplier](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html)!: the default checker to fall back to if there is no main thread checker set.

**Return**
[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html): `true` if called on the main thread, `false` if not.

`static fun onCheckMainThread(defaultChecker: `[`BooleanSupplier`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html)`!): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

Returns `true` if called on the main thread, `false` if not. This will prefer a set checker via ``[`#setOnCheckMainThread(BooleanSupplier)`](set-on-check-main-thread.md) if one is present, otherwise it will use `defaultChecker`.

### Parameters

`defaultChecker` - [BooleanSupplier](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BooleanSupplier.html)!: the default checker to fall back to if there is no main thread checker set.

**Return**
[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html): `true` if called on the main thread, `false` if not.

