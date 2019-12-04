[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [AutoDisposePlugins](./index.md)

# AutoDisposePlugins

`class AutoDisposePlugins`

Utility class to inject handlers to certain standard autodispose-lifecycle operations.

### Functions

| Name | Summary |
|---|---|
| [getFillInOutsideScopeExceptionStacktraces](get-fill-in-outside-scope-exception-stacktraces.md) | `static fun getFillInOutsideScopeExceptionStacktraces(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [getHideProxies](get-hide-proxies.md) | `static fun getHideProxies(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [getOutsideScopeHandler](get-outside-scope-handler.md) | `static fun getOutsideScopeHandler(): `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`OutsideScopeException`](../-outside-scope-exception/index.md)`!>?` |
| [isLockdown](is-lockdown.md) | `static fun isLockdown(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Returns true if the plugins were locked down. |
| [lockdown](lockdown.md) | `static fun lockdown(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Prevents changing the plugins from then on.  |
| [reset](reset.md) | `static fun reset(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Removes all handlers and resets to default behavior. |
| [setFillInOutsideScopeExceptionStacktraces](set-fill-in-outside-scope-exception-stacktraces.md) | `static fun setFillInOutsideScopeExceptionStacktraces(fillInStacktrace: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setHideProxies](set-hide-proxies.md) | `static fun setHideProxies(hideProxies: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setOutsideScopeHandler](set-outside-scope-handler.md) | `static fun setOutsideScopeHandler(handler: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`OutsideScopeException`](../-outside-scope-exception/index.md)`!>?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
