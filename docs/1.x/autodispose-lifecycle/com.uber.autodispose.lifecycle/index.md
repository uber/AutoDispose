[autodispose-lifecycle](../index.md) / [com.uber.autodispose.lifecycle](./index.md)

## Package com.uber.autodispose.lifecycle

### Types

| Name | Summary |
|---|---|
| [CorrespondingEventsFunction](-corresponding-events-function/index.md) | `interface CorrespondingEventsFunction<E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : `[`Function`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function.html)`<`[`E`](-corresponding-events-function/index.md#E)`, `[`E`](-corresponding-events-function/index.md#E)`>`<br>A corresponding events function that acts as a normal ``[`Function`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function.html) but ensures a single event type in the generic and tightens the possible exception thrown to ``[`OutsideScopeException`](#). |
| [LifecycleScopeProvider](-lifecycle-scope-provider/index.md) | `interface LifecycleScopeProvider<E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : ScopeProvider`<br>A convenience interface that, when implemented, helps provide information to create implementations that resolve the next corresponding lifecycle event and construct a ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representation of it from the ``[`#lifecycle()`](-lifecycle-scope-provider/lifecycle.md) stream.  |
| [LifecycleScopes](-lifecycle-scopes/index.md) | `class LifecycleScopes`<br>Utilities for dealing with ``[`LifecycleScopeProvider`](-lifecycle-scope-provider/index.md)s. This includes factories for resolving ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representations of scopes, corresponding events, etc. |
| [TestLifecycleScopeProvider](-test-lifecycle-scope-provider/index.md) | `class TestLifecycleScopeProvider : `[`LifecycleScopeProvider`](-lifecycle-scope-provider/index.md)`<`[`TestLifecycleScopeProvider.TestLifecycle`](-test-lifecycle-scope-provider/-test-lifecycle/index.md)`!>`<br>Test utility to create ``[`LifecycleScopeProvider`](-lifecycle-scope-provider/index.md) instances for tests.  |

### Exceptions

| Name | Summary |
|---|---|
| [LifecycleEndedException](-lifecycle-ended-exception/index.md) | `open class LifecycleEndedException : OutsideScopeException`<br>Signifies an error occurred due to execution starting after the lifecycle has ended. |
| [LifecycleNotStartedException](-lifecycle-not-started-exception/index.md) | `open class LifecycleNotStartedException : OutsideScopeException`<br>Signifies an error occurred due to execution starting before the lifecycle has started. |
