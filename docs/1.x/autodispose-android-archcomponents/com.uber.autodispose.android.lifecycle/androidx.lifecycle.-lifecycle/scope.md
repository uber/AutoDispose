[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [androidx.lifecycle.Lifecycle](index.md) / [scope](./scope.md)

# scope

`@CheckReturnValue inline fun Lifecycle.scope(): ScopeProvider`

Extension that returns a [ScopeProvider](#) for this [Lifecycle](#).

`@CheckReturnValue inline fun Lifecycle.scope(untilEvent: Event): ScopeProvider`

Extension that returns a [ScopeProvider](#) for this [Lifecycle](#).

### Parameters

`untilEvent` - the event until the scope is valid.`@CheckReturnValue inline fun Lifecycle.scope(boundaryResolver: CorrespondingEventsFunction<Event>): ScopeProvider`

Extension that returns a [ScopeProvider](#) for this [Lifecycle](#).

### Parameters

`boundaryResolver` - function that resolves the event boundary.