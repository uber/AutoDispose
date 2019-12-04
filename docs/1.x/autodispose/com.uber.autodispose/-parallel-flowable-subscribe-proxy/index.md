[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [ParallelFlowableSubscribeProxy](./index.md)

# ParallelFlowableSubscribeProxy

`interface ParallelFlowableSubscribeProxy<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!>`

Subscribe proxy that matches ``[`ParallelFlowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html)'s subscribe overloads.

### Functions

| Name | Summary |
|---|---|
| [subscribe](subscribe.md) | `abstract fun subscribe(subscribers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<Subscriber<in `[`T`](index.md#T)`>!>!): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Proxy for ``[`ParallelFlowable#subscribe(Subscriber[])`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html#subscribe(org.reactivestreams.Subscriber<? super T>[])). |
