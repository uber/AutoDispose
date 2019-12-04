[autodispose-lifecycle](../../index.md) / [com.uber.autodispose.lifecycle](../index.md) / [TestLifecycleScopeProvider](./index.md)

# TestLifecycleScopeProvider

`class TestLifecycleScopeProvider : `[`LifecycleScopeProvider`](../-lifecycle-scope-provider/index.md)`<`[`TestLifecycleScopeProvider.TestLifecycle`](-test-lifecycle/index.md)`!>`

Test utility to create ``[`LifecycleScopeProvider`](../-lifecycle-scope-provider/index.md) instances for tests.

Supports a start and stop lifecycle. Subscribing when outside of the lifecycle will throw either a ``[`LifecycleNotStartedException`](../-lifecycle-not-started-exception/index.md) or ``[`LifecycleEndedException`](../-lifecycle-ended-exception/index.md).

### Types

| Name | Summary |
|---|---|
| [TestLifecycle](-test-lifecycle/index.md) | `class TestLifecycle` |

### Functions

| Name | Summary |
|---|---|
| [correspondingEvents](corresponding-events.md) | `fun correspondingEvents(): `[`CorrespondingEventsFunction`](../-corresponding-events-function/index.md)`<`[`TestLifecycleScopeProvider.TestLifecycle`](-test-lifecycle/index.md)`!>!` |
| [create](create.md) | `static fun create(): `[`TestLifecycleScopeProvider`](./index.md)`!` |
| [createInitial](create-initial.md) | `static fun createInitial(initialValue: `[`TestLifecycleScopeProvider.TestLifecycle`](-test-lifecycle/index.md)`!): `[`TestLifecycleScopeProvider`](./index.md)`!` |
| [lifecycle](lifecycle.md) | `fun lifecycle(): `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`TestLifecycleScopeProvider.TestLifecycle`](-test-lifecycle/index.md)`!>!` |
| [peekLifecycle](peek-lifecycle.md) | `fun peekLifecycle(): `[`TestLifecycleScopeProvider.TestLifecycle`](-test-lifecycle/index.md)`?` |
| [requestScope](request-scope.md) | `fun requestScope(): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!` |
| [start](start.md) | `fun start(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Start the test lifecycle. |
| [stop](stop.md) | `fun stop(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Stop the test lifecycle. |
