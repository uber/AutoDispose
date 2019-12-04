[autodispose](../index.md) / [com.uber.autodispose](./index.md)

## Package com.uber.autodispose

Core implementation.

### Types

| Name | Summary |
|---|---|
| [AutoDispose](-auto-dispose/index.md) | `class AutoDispose`<br>Factories for autodispose converters that can be used with RxJava types' corresponding `as(...)` methods to transform them into auto-disposing streams.  |
| [AutoDisposeContext](-auto-dispose-context/index.md) | `interface AutoDisposeContext`<br>A context intended for use as `AutoDisposeContext.() -> Unit` function body parameters where zero-arg [autoDispose](-auto-dispose-context/auto-dispose.md) functions can be called. This should be backed by an underlying [Completable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) or [ScopeProvider](-scope-provider/index.md). |
| [AutoDisposeConverter](-auto-dispose-converter.md) | `interface AutoDisposeConverter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : `[`FlowableConverter`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableConverter.html)`<`[`T`](-auto-dispose-converter.md#T)`, `[`FlowableSubscribeProxy`](-flowable-subscribe-proxy/index.md)`<`[`T`](-auto-dispose-converter.md#T)`>!>, `[`ParallelFlowableConverter`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowableConverter.html)`<`[`T`](-auto-dispose-converter.md#T)`, `[`ParallelFlowableSubscribeProxy`](-parallel-flowable-subscribe-proxy/index.md)`<`[`T`](-auto-dispose-converter.md#T)`>!>, `[`ObservableConverter`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/ObservableConverter.html)`<`[`T`](-auto-dispose-converter.md#T)`, `[`ObservableSubscribeProxy`](-observable-subscribe-proxy/index.md)`<`[`T`](-auto-dispose-converter.md#T)`>!>, `[`MaybeConverter`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/MaybeConverter.html)`<`[`T`](-auto-dispose-converter.md#T)`, `[`MaybeSubscribeProxy`](-maybe-subscribe-proxy/index.md)`<`[`T`](-auto-dispose-converter.md#T)`>!>, `[`SingleConverter`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleConverter.html)`<`[`T`](-auto-dispose-converter.md#T)`, `[`SingleSubscribeProxy`](-single-subscribe-proxy/index.md)`<`[`T`](-auto-dispose-converter.md#T)`>!>, `[`CompletableConverter`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableConverter.html)`<`[`CompletableSubscribeProxy`](-completable-subscribe-proxy/index.md)`!>`<br>A custom converter that implements all the RxJava types converters, for use with the `as()` operator. |
| [AutoDisposePlugins](-auto-dispose-plugins/index.md) | `class AutoDisposePlugins`<br>Utility class to inject handlers to certain standard autodispose-lifecycle operations. |
| [CompletableSubscribeProxy](-completable-subscribe-proxy/index.md) | `interface CompletableSubscribeProxy`<br>Subscribe proxy that matches ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)'s subscribe overloads. |
| [FlowableSubscribeProxy](-flowable-subscribe-proxy/index.md) | `interface FlowableSubscribeProxy<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!>`<br>Subscribe proxy that matches ``[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)'s subscribe overloads. |
| [MaybeSubscribeProxy](-maybe-subscribe-proxy/index.md) | `interface MaybeSubscribeProxy<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!>`<br>Subscribe proxy that matches ``[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)'s subscribe overloads. |
| [ObservableSubscribeProxy](-observable-subscribe-proxy/index.md) | `interface ObservableSubscribeProxy<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!>`<br>Subscribe proxy that matches ``[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)'s subscribe overloads. |
| [ParallelFlowableSubscribeProxy](-parallel-flowable-subscribe-proxy/index.md) | `interface ParallelFlowableSubscribeProxy<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!>`<br>Subscribe proxy that matches ``[`ParallelFlowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html)'s subscribe overloads. |
| [ScopeProvider](-scope-provider/index.md) | `interface ScopeProvider`<br>Provides a ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html) representation of a scope. The emission of this is the signal |
| [Scopes](-scopes/index.md) | `class Scopes`<br>Utilities for dealing with AutoDispose scopes. |
| [SingleSubscribeProxy](-single-subscribe-proxy/index.md) | `interface SingleSubscribeProxy<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!>`<br>Subscribe proxy that matches ``[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)'s subscribe overloads. |
| [TestScopeProvider](-test-scope-provider/index.md) | `class TestScopeProvider : `[`ScopeProvider`](-scope-provider/index.md)<br>ScopeProvider implementation for testing. You can either back it with your own instance, or just stub it in place and use its public emit APIs. |

### Exceptions

| Name | Summary |
|---|---|
| [OutsideScopeException](-outside-scope-exception/index.md) | `open class OutsideScopeException : `[`RuntimeException`](https://docs.oracle.com/javase/6/docs/api/java/lang/RuntimeException.html)<br>Signifies an error occurred due to execution starting outside the lifecycle. |

### Extensions for External Classes

| Name | Summary |
|---|---|
| [io.reactivex.Completable](io.reactivex.-completable/index.md) |  |
| [io.reactivex.Flowable](io.reactivex.-flowable/index.md) |  |
| [io.reactivex.Maybe](io.reactivex.-maybe/index.md) |  |
| [io.reactivex.Observable](io.reactivex.-observable/index.md) |  |
| [io.reactivex.parallel.ParallelFlowable](io.reactivex.parallel.-parallel-flowable/index.md) |  |
| [io.reactivex.Single](io.reactivex.-single/index.md) |  |

### Functions

| Name | Summary |
|---|---|
| [withScope](with-scope.md) | `fun withScope(scope: `[`ScopeProvider`](-scope-provider/index.md)`, body: `[`AutoDisposeContext`](-auto-dispose-context/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Executes a [body](with-scope.md#com.uber.autodispose$withScope(com.uber.autodispose.ScopeProvider, kotlin.Function1((com.uber.autodispose.AutoDisposeContext, kotlin.Unit)))/body) with an [AutoDisposeContext](-auto-dispose-context/index.md) backed by the given [scope](with-scope.md#com.uber.autodispose$withScope(com.uber.autodispose.ScopeProvider, kotlin.Function1((com.uber.autodispose.AutoDisposeContext, kotlin.Unit)))/scope).`fun withScope(completableScope: `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`, body: `[`AutoDisposeContext`](-auto-dispose-context/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Executes a [body](with-scope.md#com.uber.autodispose$withScope(io.reactivex.Completable, kotlin.Function1((com.uber.autodispose.AutoDisposeContext, kotlin.Unit)))/body) with an [AutoDisposeContext](-auto-dispose-context/index.md) backed by the given [completableScope](with-scope.md#com.uber.autodispose$withScope(io.reactivex.Completable, kotlin.Function1((com.uber.autodispose.AutoDisposeContext, kotlin.Unit)))/completableScope). |
