[autodispose-rxlifecycle](../../index.md) / [com.ubercab.autodispose.rxlifecycle](../index.md) / [RxLifecycleInterop](./index.md)

# RxLifecycleInterop

`class RxLifecycleInterop`

Interop for RxLifecycle. This provides static factory methods to convert s into ``[`ScopeProvider`](#) representations.

*Note:* RxLifecycle treats the ``[`OutsideLifecycleException`](#) as normal terminal event. In such cases the stream is just disposed.

### Functions

| Name | Summary |
|---|---|
| [from](from.md) | `static fun <E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> from(provider: LifecycleProvider<`[`E`](from.md#E)`>!): ScopeProvider!`<br>`static fun <E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> from(provider: LifecycleProvider<`[`E`](from.md#E)`>!, event: `[`E`](from.md#E)`): ScopeProvider!`<br>Factory creating a ``[`ScopeProvider`](#) representation of a ``[`LifecycleProvider`](#).  |
