[autodispose-android-archcomponents](../../index.md) / [com.uber.autodispose.android.lifecycle](../index.md) / [androidx.lifecycle.LifecycleOwner](index.md) / [scope](./scope.md)

# scope

`@CheckReturnValue inline fun LifecycleOwner.scope(): ScopeProvider`

Extension that returns a [ScopeProvider](#) for this [LifecycleOwner](#).

`@CheckReturnValue inline fun LifecycleOwner.scope(untilEvent: Event): ScopeProvider`

Extension that returns a [ScopeProvider](#) for this [LifecycleOwner](#).

### Parameters

`untilEvent` - the event until the scope is valid.`@CheckReturnValue inline fun LifecycleOwner.scope(boundaryResolver: CorrespondingEventsFunction<Event>): ScopeProvider`

Extension that returns a [ScopeProvider](#) for this [LifecycleOwner](#).

### Parameters

`boundaryResolver` - function that resolves the event boundary.