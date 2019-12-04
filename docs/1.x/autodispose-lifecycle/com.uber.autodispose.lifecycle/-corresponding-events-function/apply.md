[autodispose-lifecycle](../../index.md) / [com.uber.autodispose.lifecycle](../index.md) / [CorrespondingEventsFunction](index.md) / [apply](./apply.md)

# apply

`abstract fun apply(event: `[`E`](index.md#E)`): `[`E`](index.md#E)

Given an event `event`, returns the next corresponding event that this lifecycle should dispose on.

### Parameters

`event` - [E](index.md#E): the source or start event.

### Exceptions

`OutsideScopeException` - if the lifecycle exceeds its scope boundaries.

**Return**
[E](index.md#E): the target event that should signal disposal.

