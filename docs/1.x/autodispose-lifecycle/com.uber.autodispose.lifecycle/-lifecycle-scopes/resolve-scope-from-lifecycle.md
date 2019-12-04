[autodispose-lifecycle](../../index.md) / [com.uber.autodispose.lifecycle](../index.md) / [LifecycleScopes](index.md) / [resolveScopeFromLifecycle](./resolve-scope-from-lifecycle.md)

# resolveScopeFromLifecycle

`static fun <E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> resolveScopeFromLifecycle(provider: `[`LifecycleScopeProvider`](../-lifecycle-scope-provider/index.md)`<`[`E`](resolve-scope-from-lifecycle.md#E)`>!): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!`

Overload for resolving lifecycle providers that defaults to checking start and end boundaries of lifecycles. That is, they will ensure that the lifecycle has both started and not ended.

*Note:* This resolves the scope immediately, so consider deferring execution as needed, such as using ``[`defer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html#defer(java.util.concurrent.Callable<? extends io.reactivex.CompletableSource>)).

### Parameters

`provider` - [LifecycleScopeProvider](../-lifecycle-scope-provider/index.md)&lt;[E](resolve-scope-from-lifecycle.md#E)&gt;!: the ``[`LifecycleScopeProvider`](../-lifecycle-scope-provider/index.md) to resolve.

`<E>` - the lifecycle event type

### Exceptions

`OutsideScopeException` - if the ``[`LifecycleScopeProvider#correspondingEvents()`](../-lifecycle-scope-provider/corresponding-events.md) throws an ``[`OutsideScopeException`](#) during resolution.

**Return**
[CompletableSource](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)!: a resolved ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html) representation of a given provider

`static fun <E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> resolveScopeFromLifecycle(provider: `[`LifecycleScopeProvider`](../-lifecycle-scope-provider/index.md)`<`[`E`](resolve-scope-from-lifecycle.md#E)`>!, checkEndBoundary: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!`

Overload for resolving lifecycle providers allows configuration of checking "end" boundaries of lifecycles. That is, they will ensure that the lifecycle has both started and not ended, and otherwise will throw one of ``[`LifecycleNotStartedException`](../-lifecycle-not-started-exception/index.md) (if ``[` `](../-lifecycle-scope-provider/peek-lifecycle.md) returns `null`) or if the lifecycle is ended. To configure the runtime behavior of these exceptions, see ``[`AutoDisposePlugins`](#).

*Note:* This resolves the scope immediately, so consider deferring execution as needed, such as using ``[`defer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html#defer(java.util.concurrent.Callable<? extends io.reactivex.CompletableSource>)).

### Parameters

`provider` - [LifecycleScopeProvider](../-lifecycle-scope-provider/index.md)&lt;[E](resolve-scope-from-lifecycle.md#E)&gt;!: the ``[`LifecycleScopeProvider`](../-lifecycle-scope-provider/index.md) to resolve.

`checkEndBoundary` - [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html): whether or not to check that the lifecycle has ended

`<E>` - the lifecycle event type

### Exceptions

`OutsideScopeException` - if the ``[`LifecycleScopeProvider#correspondingEvents()`](../-lifecycle-scope-provider/corresponding-events.md) throws an ``[`OutsideScopeException`](#) during resolution.

**Return**
[CompletableSource](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)!: a resolved ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html) representation of a given provider

`static fun <E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> resolveScopeFromLifecycle(lifecycle: `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`E`](resolve-scope-from-lifecycle.md#E)`>!, endEvent: `[`E`](resolve-scope-from-lifecycle.md#E)`): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!`

### Parameters

`lifecycle` - [Observable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)&lt;[E](resolve-scope-from-lifecycle.md#E)&gt;!: the stream of lifecycle events

`endEvent` - [E](resolve-scope-from-lifecycle.md#E): the target end event

`<E>` - the lifecycle event type

**Return**
[CompletableSource](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)!: a resolved ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representation of a given lifecycle, targeting the given event

`static fun <E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> resolveScopeFromLifecycle(lifecycle: `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`E`](resolve-scope-from-lifecycle.md#E)`>!, endEvent: `[`E`](resolve-scope-from-lifecycle.md#E)`, @Nullable comparator: `[`Comparator`](https://docs.oracle.com/javase/6/docs/api/java/util/Comparator.html)`<`[`E`](resolve-scope-from-lifecycle.md#E)`>?): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!`

### Parameters

`lifecycle` - [Observable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)&lt;[E](resolve-scope-from-lifecycle.md#E)&gt;!: the stream of lifecycle events

`endEvent` - [E](resolve-scope-from-lifecycle.md#E): the target end event

`comparator` - [Comparator](https://docs.oracle.com/javase/6/docs/api/java/util/Comparator.html)&lt;[E](resolve-scope-from-lifecycle.md#E)&gt;?: an optional comparator for checking event equality.

`<E>` - the lifecycle event type

**Return**
[CompletableSource](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)!: a resolved ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representation of a given lifecycle, targeting the given event

