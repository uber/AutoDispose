[autodispose](../../index.md) / [com.uber.autodispose.observers](../index.md) / [AutoDisposingObserver](./index.md)

# AutoDisposingObserver

`interface AutoDisposingObserver<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : `[`Observer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observer.html)`<`[`T`](index.md#T)`>, `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`Observer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observer.html) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation.

### Functions

| Name | Summary |
|---|---|
| [delegateObserver](delegate-observer.md) | `abstract fun delegateObserver(): `[`Observer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observer.html)`<in `[`T`](index.md#T)`>!` |
