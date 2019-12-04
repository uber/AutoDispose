[autodispose](../../index.md) / [com.uber.autodispose.observers](../index.md) / [AutoDisposingMaybeObserver](./index.md)

# AutoDisposingMaybeObserver

`interface AutoDisposingMaybeObserver<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : `[`MaybeObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/MaybeObserver.html)`<`[`T`](index.md#T)`>, `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`MaybeObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/MaybeObserver.html) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation.

### Functions

| Name | Summary |
|---|---|
| [delegateObserver](delegate-observer.md) | `abstract fun delegateObserver(): `[`MaybeObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/MaybeObserver.html)`<in `[`T`](index.md#T)`>!` |
