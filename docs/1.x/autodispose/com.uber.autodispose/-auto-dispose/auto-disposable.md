[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [AutoDispose](index.md) / [autoDisposable](./auto-disposable.md)

# autoDisposable

`static fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> autoDisposable(provider: `[`ScopeProvider`](../-scope-provider/index.md)`!): `[`AutoDisposeConverter`](../-auto-dispose-converter.md)`<`[`T`](auto-disposable.md#T)`>!`

Entry point for auto-disposing streams from a ``[`ScopeProvider`](../-scope-provider/index.md).

Example usage:

```

      Observable.just(1)
           .as(autoDisposable(scope)) // Static import
           .subscribe(...)
    ```

### Parameters

`provider` - [ScopeProvider](../-scope-provider/index.md)!: the target scope provider

`<T>` - the stream type.

**Return**
[AutoDisposeConverter](../-auto-dispose-converter.md)&lt;[T](auto-disposable.md#T)&gt;!: an ``[`AutoDisposeConverter`](../-auto-dispose-converter.md) to transform with operators like ``[` `](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#as(io.reactivex.ObservableConverter<T,? extends R>))

`static fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> autoDisposable(scope: `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!): `[`AutoDisposeConverter`](../-auto-dispose-converter.md)`<`[`T`](auto-disposable.md#T)`>!`

Entry point for auto-disposing streams from a ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html).

Example usage:

```

      Observable.just(1)
           .as(autoDisposable(scope)) // Static import
           .subscribe(...)
    ```

### Parameters

`scope` - [CompletableSource](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)!: the target scope

`<T>` - the stream type.

**Return**
[AutoDisposeConverter](../-auto-dispose-converter.md)&lt;[T](auto-disposable.md#T)&gt;!: an ``[`AutoDisposeConverter`](../-auto-dispose-converter.md) to transform with operators like ``[` `](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#as(io.reactivex.ObservableConverter<T,? extends R>))

