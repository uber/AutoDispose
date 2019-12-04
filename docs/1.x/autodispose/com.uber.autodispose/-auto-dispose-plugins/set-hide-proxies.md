[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [AutoDisposePlugins](index.md) / [setHideProxies](./set-hide-proxies.md)

# setHideProxies

`static fun setHideProxies(hideProxies: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

### Parameters

`hideProxies` - [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html): `true` hide proxy interfaces. This wraps all proxy interfaces in ``[`com.uber.autodispose`](../index.md) at runtime in an anonymous instance to prevent introspection, similar to ``[`Observable#hide()`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#hide()). The default is `true`.