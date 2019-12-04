[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [TestScopeProvider](./index.md)

# TestScopeProvider

`class TestScopeProvider : `[`ScopeProvider`](../-scope-provider/index.md)

ScopeProvider implementation for testing. You can either back it with your own instance, or just stub it in place and use its public emit APIs.

### Inherited Properties

| Name | Summary |
|---|---|
| [UNBOUND](../-scope-provider/-u-n-b-o-u-n-d.md) | `static val UNBOUND: `[`ScopeProvider`](../-scope-provider/index.md)`!`<br>A new provider that is "unbound", e.g. will emit a completion event to signal that the scope is unbound. |

### Functions

| Name | Summary |
|---|---|
| [create](create.md) | `static fun create(): `[`TestScopeProvider`](./index.md)`!`<br>Creates a new provider backed by an internal ``[`CompletableSubject`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/subjects/CompletableSubject.html). Useful for stubbing or if you only want to use the emit APIs`static fun create(delegate: `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`!): `[`TestScopeProvider`](./index.md)`!`<br>Creates a new provider backed by `delegate`. |
| [emit](emit.md) | `fun emit(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Emits a success event, just a simple Object. |
| [requestScope](request-scope.md) | `fun requestScope(): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!` |
