[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [ScopeProvider](index.md) / [requestScope](./request-scope.md)

# requestScope

`@CheckReturnValue abstract fun requestScope(): `[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)`!`

### Exceptions

`Exception` - scope retrievals throws an exception, such as ``[`OutsideScopeException`](../-outside-scope-exception/index.md)

**Return**
[CompletableSource](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html)!: a ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html) that, upon completion, will trigger disposal.

