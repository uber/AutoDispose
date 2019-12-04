[autodispose](../../index.md) / [com.uber.autodispose.observers](../index.md) / [AutoDisposingSubscriber](./index.md)

# AutoDisposingSubscriber

`interface AutoDisposingSubscriber<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : `[`FlowableSubscriber`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableSubscriber.html)`<`[`T`](index.md#T)`>, Subscription, `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`Subscriber`](#) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation.

### Functions

| Name | Summary |
|---|---|
| [delegateSubscriber](delegate-subscriber.md) | `abstract fun delegateSubscriber(): Subscriber<in `[`T`](index.md#T)`>!` |
