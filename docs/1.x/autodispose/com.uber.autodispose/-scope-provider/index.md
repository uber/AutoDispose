[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [ScopeProvider](./index.md)

# ScopeProvider

`@DoNotMock("Use TestScopeProvider instead") interface ScopeProvider`

Provides a ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html) representation of a scope. The emission of this is the signal

### Properties

| Name | Summary |
|---|---|
| [UNBOUND](-u-n-b-o-u-n-d.md) | `static val UNBOUND: `[`ScopeProvider`](./index.md)`!`<br>A new provider that is "unbound", e.g. will emit a completion event to signal that the scope is unbound. |

### Functions

| Name | Summary |
|---|---|
| [requestScope](request-scope.md) | `abstract fun requestScope(): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!` |

### Inheritors

| Name | Summary |
|---|---|
| [TestScopeProvider](../-test-scope-provider/index.md) | `class TestScopeProvider : `[`ScopeProvider`](./index.md)<br>ScopeProvider implementation for testing. You can either back it with your own instance, or just stub it in place and use its public emit APIs. |
