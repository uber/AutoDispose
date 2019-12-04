[autodispose](../../index.md) / [com.uber.autodispose.observers](../index.md) / [AutoDisposingSingleObserver](./index.md)

# AutoDisposingSingleObserver

`interface AutoDisposingSingleObserver<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : `[`SingleObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleObserver.html)`<`[`T`](index.md#T)`>, `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`SingleObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleObserver.html) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation.

### Functions

| Name | Summary |
|---|---|
| [delegateObserver](delegate-observer.md) | `abstract fun delegateObserver(): `[`SingleObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleObserver.html)`<in `[`T`](index.md#T)`>!` |
