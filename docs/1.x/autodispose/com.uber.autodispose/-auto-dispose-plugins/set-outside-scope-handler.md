[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [AutoDisposePlugins](index.md) / [setOutsideScopeHandler](./set-outside-scope-handler.md)

# setOutsideScopeHandler

`static fun setOutsideScopeHandler(@Nullable handler: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`OutsideScopeException`](../-outside-scope-exception/index.md)`!>?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

### Parameters

`handler` - [Consumer](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)&lt;in&nbsp;[OutsideScopeException](../-outside-scope-exception/index.md)!&gt;?: the consumer for handling ``[`OutsideScopeException`](../-outside-scope-exception/index.md) to set, null allowed