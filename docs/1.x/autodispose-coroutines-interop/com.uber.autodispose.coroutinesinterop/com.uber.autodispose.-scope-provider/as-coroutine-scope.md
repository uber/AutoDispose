[autodispose-coroutines-interop](../../index.md) / [com.uber.autodispose.coroutinesinterop](../index.md) / [com.uber.autodispose.ScopeProvider](index.md) / [asCoroutineScope](./as-coroutine-scope.md)

# asCoroutineScope

`fun ScopeProvider.asCoroutineScope(context: `[`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/index.html)` = SupervisorJob()): CoroutineScope`

### Parameters

`context` - an optional [CoroutineContext](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/index.html) to use for this scope. Default is a new
    [SupervisorJob](#).

**Return**
a [CoroutineScope](#) representation of this [ScopeProvider](#). This scope will cancel when
    [this](as-coroutine-scope/-this-.md) scope provider completes.

