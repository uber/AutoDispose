[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [AutoDisposePlugins](index.md) / [setFillInOutsideScopeExceptionStacktraces](./set-fill-in-outside-scope-exception-stacktraces.md)

# setFillInOutsideScopeExceptionStacktraces

`static fun setFillInOutsideScopeExceptionStacktraces(fillInStacktrace: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

### Parameters

`fillInStacktrace` - [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html): `true` to fill in stacktraces in ``[`OutsideScopeException`](../-outside-scope-exception/index.md)s. `false` to disable them (and use them as signals only). Disabling them, if you don't care about the stacktraces, can result in some minor performance improvements.