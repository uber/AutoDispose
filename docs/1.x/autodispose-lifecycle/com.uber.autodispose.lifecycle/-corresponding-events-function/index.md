[autodispose-lifecycle](../../index.md) / [com.uber.autodispose.lifecycle](../index.md) / [CorrespondingEventsFunction](./index.md)

# CorrespondingEventsFunction

`interface CorrespondingEventsFunction<E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : `[`Function`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function.html)`<`[`E`](index.md#E)`, `[`E`](index.md#E)`>`

A corresponding events function that acts as a normal ``[`Function`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function.html) but ensures a single event type in the generic and tightens the possible exception thrown to ``[`OutsideScopeException`](#).

### Functions

| Name | Summary |
|---|---|
| [apply](apply.md) | `abstract fun apply(event: `[`E`](index.md#E)`): `[`E`](index.md#E)<br>Given an event `event`, returns the next corresponding event that this lifecycle should dispose on. |
