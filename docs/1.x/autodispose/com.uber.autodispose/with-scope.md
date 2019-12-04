[autodispose](../index.md) / [com.uber.autodispose](index.md) / [withScope](./with-scope.md)

# withScope

`inline fun withScope(scope: `[`ScopeProvider`](-scope-provider/index.md)`, body: `[`AutoDisposeContext`](-auto-dispose-context/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Executes a [body](with-scope.md#com.uber.autodispose$withScope(com.uber.autodispose.ScopeProvider, kotlin.Function1((com.uber.autodispose.AutoDisposeContext, kotlin.Unit)))/body) with an [AutoDisposeContext](-auto-dispose-context/index.md) backed by the given [scope](with-scope.md#com.uber.autodispose$withScope(com.uber.autodispose.ScopeProvider, kotlin.Function1((com.uber.autodispose.AutoDisposeContext, kotlin.Unit)))/scope).

`inline fun withScope(completableScope: `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`, body: `[`AutoDisposeContext`](-auto-dispose-context/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Executes a [body](with-scope.md#com.uber.autodispose$withScope(io.reactivex.Completable, kotlin.Function1((com.uber.autodispose.AutoDisposeContext, kotlin.Unit)))/body) with an [AutoDisposeContext](-auto-dispose-context/index.md) backed by the given [completableScope](with-scope.md#com.uber.autodispose$withScope(io.reactivex.Completable, kotlin.Function1((com.uber.autodispose.AutoDisposeContext, kotlin.Unit)))/completableScope).

