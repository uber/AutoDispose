[autodispose-coroutines-interop](../../index.md) / [com.uber.autodispose.coroutinesinterop](../index.md) / [io.reactivex.CompletableSource](index.md) / [asCoroutineScope](./as-coroutine-scope.md)

# asCoroutineScope

`fun `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`.asCoroutineScope(context: `[`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/index.html)` = SupervisorJob()): CoroutineScope`

### Parameters

`context` - an optional [CoroutineContext](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/index.html) to use for this scope. Default is a new
    [SupervisorJob](#).

**Return**
a [CoroutineScope](#) representation of this [CompletableSource](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html). This scope will cancel
    when [this](as-coroutine-scope/-this-.md) scope provider completes.

