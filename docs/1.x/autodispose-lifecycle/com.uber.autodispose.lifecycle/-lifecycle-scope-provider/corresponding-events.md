[autodispose-lifecycle](../../index.md) / [com.uber.autodispose.lifecycle](../index.md) / [LifecycleScopeProvider](index.md) / [correspondingEvents](./corresponding-events.md)

# correspondingEvents

`@CheckReturnValue abstract fun correspondingEvents(): `[`CorrespondingEventsFunction`](../-corresponding-events-function/index.md)`<`[`E`](index.md#E)`>!`

**Return**
[CorrespondingEventsFunction](../-corresponding-events-function/index.md)&lt;[E](index.md#E)&gt;!: a sequence of lifecycle events. It's recommended to back this with a static instance to avoid unnecessary object allocation.

