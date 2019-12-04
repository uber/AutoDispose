[autodispose-rxlifecycle](../../index.md) / [com.ubercab.autodispose.rxlifecycle](../index.md) / [RxLifecycleInterop](index.md) / [from](./from.md)

# from

`static fun <E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> from(provider: LifecycleProvider<`[`E`](from.md#E)`>!): ScopeProvider!`

Factory creating a ``[`ScopeProvider`](#) representation of a ``[`LifecycleProvider`](#).

Example usage:

```

      Observable.just(1)
           .as(autoDisposable(RxLifecycleInterop.from(lifecycleProvider)))
           .subscribe(...)
    ```

### Parameters

`<E>` - the lifecycle event.

`provider` - LifecycleProvider&lt;[E](from.md#E)&gt;!: the ``[`LifecycleProvider`](#).

**Return**
ScopeProvider!: a ``[`ScopeProvider`](#)

`static fun <E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> from(provider: LifecycleProvider<`[`E`](from.md#E)`>!, event: `[`E`](from.md#E)`): ScopeProvider!`

Factory creating a ``[`ScopeProvider`](#) representation of a ``[`LifecycleProvider`](#).

Example usage:

```

      Observable.just(1)
           .as(autoDisposable(RxLifecycleInterop.from(lifecycleProvider, event)))
           .subscribe(...)
    ```

### Parameters

`<E>` - the lifecycle event.

`provider` - LifecycleProvider&lt;[E](from.md#E)&gt;!: the ``[`LifecycleProvider`](#).

`event` - [E](from.md#E): a target event to dispose upon.

**Return**
ScopeProvider!: a ``[`ScopeProvider`](#)

