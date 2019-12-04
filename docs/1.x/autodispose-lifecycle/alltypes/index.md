

```
public interface LifecycleScopeProvider<E> extends ScopeProvider {
Observable<E> lifecycle();

Function<E, E> correspondingEvents();

E peekLifecycle();

// Inherited from ScopeProvider
CompletableSource requestScope();
}
```

### All Types

| Name | Summary |
|---|---|
| [com.uber.autodispose.lifecycle.CorrespondingEventsFunction](../com.uber.autodispose.lifecycle/-corresponding-events-function/index.md) | A corresponding events function that acts as a normal ``[`Function`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function.html) but ensures a single event type in the generic and tightens the possible exception thrown to ``[`OutsideScopeException`](#). |
| [com.uber.autodispose.lifecycle.LifecycleEndedException](../com.uber.autodispose.lifecycle/-lifecycle-ended-exception/index.md) | Signifies an error occurred due to execution starting after the lifecycle has ended. |
| [com.uber.autodispose.lifecycle.LifecycleNotStartedException](../com.uber.autodispose.lifecycle/-lifecycle-not-started-exception/index.md) | Signifies an error occurred due to execution starting before the lifecycle has started. |
| [com.uber.autodispose.lifecycle.LifecycleScopeProvider](../com.uber.autodispose.lifecycle/-lifecycle-scope-provider/index.md) | A convenience interface that, when implemented, helps provide information to create implementations that resolve the next corresponding lifecycle event and construct a ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representation of it from the ``[`#lifecycle()`](../com.uber.autodispose.lifecycle/-lifecycle-scope-provider/lifecycle.md) stream.  |
| [com.uber.autodispose.lifecycle.LifecycleScopes](../com.uber.autodispose.lifecycle/-lifecycle-scopes/index.md) | Utilities for dealing with ``[`LifecycleScopeProvider`](../com.uber.autodispose.lifecycle/-lifecycle-scope-provider/index.md)s. This includes factories for resolving ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representations of scopes, corresponding events, etc. |
| [com.uber.autodispose.lifecycle.TestLifecycleScopeProvider](../com.uber.autodispose.lifecycle/-test-lifecycle-scope-provider/index.md) | Test utility to create ``[`LifecycleScopeProvider`](../com.uber.autodispose.lifecycle/-lifecycle-scope-provider/index.md) instances for tests.  |
