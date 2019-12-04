[autodispose-lifecycle](../../index.md) / [com.uber.autodispose.lifecycle](../index.md) / [LifecycleScopeProvider](./index.md)

# LifecycleScopeProvider

`@DoNotMock("Use TestLifecycleScopeProvider instead") interface LifecycleScopeProvider<E : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`!> : ScopeProvider`

A convenience interface that, when implemented, helps provide information to create implementations that resolve the next corresponding lifecycle event and construct a ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representation of it from the ``[`#lifecycle()`](lifecycle.md) stream.

Convenience resolver utilities for this can be found in ``[`LifecycleScopes`](../-lifecycle-scopes/index.md).

**See Also**
[LifecycleScopes](../-lifecycle-scopes/index.md)

### Functions

| Name | Summary |
|---|---|
| [correspondingEvents](corresponding-events.md) | `abstract fun correspondingEvents(): `[`CorrespondingEventsFunction`](../-corresponding-events-function/index.md)`<`[`E`](index.md#E)`>!` |
| [lifecycle](lifecycle.md) | `abstract fun lifecycle(): `[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)`<`[`E`](index.md#E)`>!` |
| [peekLifecycle](peek-lifecycle.md) | `abstract fun peekLifecycle(): `[`E`](index.md#E)`?` |
| [requestScope](request-scope.md) | `open fun requestScope(): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!` |

### Inheritors

| Name | Summary |
|---|---|
| [TestLifecycleScopeProvider](../-test-lifecycle-scope-provider/index.md) | `class TestLifecycleScopeProvider : `[`LifecycleScopeProvider`](./index.md)`<`[`TestLifecycleScopeProvider.TestLifecycle`](../-test-lifecycle-scope-provider/-test-lifecycle/index.md)`!>`<br>Test utility to create ``[`LifecycleScopeProvider`](./index.md) instances for tests.  |
