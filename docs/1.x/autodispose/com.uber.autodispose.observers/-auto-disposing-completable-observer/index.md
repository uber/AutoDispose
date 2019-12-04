[autodispose](../../index.md) / [com.uber.autodispose.observers](../index.md) / [AutoDisposingCompletableObserver](./index.md)

# AutoDisposingCompletableObserver

`interface AutoDisposingCompletableObserver : `[`CompletableObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableObserver.html)`, `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`CompletableObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableObserver.html) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation.

### Functions

| Name | Summary |
|---|---|
| [delegateObserver](delegate-observer.md) | `abstract fun delegateObserver(): `[`CompletableObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableObserver.html)`!` |
