[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [AutoDispose](./index.md)

# AutoDispose

`class AutoDispose`

Factories for autodispose converters that can be used with RxJava types' corresponding `as(...)` methods to transform them into auto-disposing streams.

There are several static `autoDisposable(...)` entry points, with the most basic being a simple ``[`#autoDisposable(CompletableSource)`](auto-disposable.md). The provided ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html) is ultimately what every scope resolves to under the hood, and AutoDispose has some built-in understanding for predefined types. The scope is considered ended upon onComplete emission of this ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html).

This is structured in such a way to be friendly to autocompletion in IDEs, where the no-parameter generic method will autocomplete with the appropriate generic parameters in Java &lt;7, or implicitly in &gt;=8.

**See Also**
[Flowable#as(io.reactivex.FlowableConverter)](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#as(io.reactivex.FlowableConverter<T,? extends R>))[Observable#as(io.reactivex.ObservableConverter)](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#as(io.reactivex.ObservableConverter<T,? extends R>))[Maybe#as(io.reactivex.MaybeConverter)](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html#as(io.reactivex.MaybeConverter<T,? extends R>))[Single#as(io.reactivex.SingleConverter)](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html#as(io.reactivex.SingleConverter<T,? extends R>))[Completable#as(io.reactivex.CompletableConverter)](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html#as(io.reactivex.CompletableConverter<? extends R>))

### Functions

| Name | Summary |
|---|---|
| [autoDisposable](auto-disposable.md) | `static fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> autoDisposable(provider: `[`ScopeProvider`](../-scope-provider/index.md)`!): `[`AutoDisposeConverter`](../-auto-dispose-converter.md)`<`[`T`](auto-disposable.md#T)`>!`<br>Entry point for auto-disposing streams from a ``[`ScopeProvider`](../-scope-provider/index.md). `static fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> autoDisposable(scope: `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!): `[`AutoDisposeConverter`](../-auto-dispose-converter.md)`<`[`T`](auto-disposable.md#T)`>!`<br>Entry point for auto-disposing streams from a ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html).  |
