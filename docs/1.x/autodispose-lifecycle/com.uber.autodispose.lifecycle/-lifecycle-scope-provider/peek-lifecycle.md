[autodispose-lifecycle](../../index.md) / [com.uber.autodispose.lifecycle](../index.md) / [LifecycleScopeProvider](index.md) / [peekLifecycle](./peek-lifecycle.md)

# peekLifecycle

`@Nullable abstract fun peekLifecycle(): `[`E`](index.md#E)`?`

**Return**
[E](index.md#E)?: the last seen lifecycle event, or `null` if none. Note that is `null` is returned at subscribe-time, it will be used as a signal to throw a .

