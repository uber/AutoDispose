[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [TestScopeProvider](index.md) / [create](./create.md)

# create

`static fun create(): `[`TestScopeProvider`](index.md)`!`

Creates a new provider backed by an internal ``[`CompletableSubject`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/subjects/CompletableSubject.html). Useful for stubbing or if you only want to use the emit APIs

**Return**
[TestScopeProvider](index.md)!: the created TestScopeProvider.

`static fun create(delegate: `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`!): `[`TestScopeProvider`](index.md)`!`

Creates a new provider backed by `delegate`.

### Parameters

`delegate` - [Completable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)!: the delegate to back this with.

**Return**
[TestScopeProvider](index.md)!: the created TestScopeProvider.

