[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [Scopes](index.md) / [completableOf](./completable-of.md)

# completableOf

`static fun completableOf(scopeProvider: `[`ScopeProvider`](../-scope-provider/index.md)`!): `[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)`!`

**Return**
[Completable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)!: a ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) representation of the given `scopeProvider`. This will be deferred appropriately and handle ``[`OutsideScopeExceptions`](../-outside-scope-exception/index.md).

