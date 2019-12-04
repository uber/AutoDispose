[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [CompletableSubscribeProxy](index.md) / [subscribe](./subscribe.md)

# subscribe

`abstract fun subscribe(): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Completable#subscribe()`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html#subscribe()).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(action: `[`Action`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Action.html)`!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Completable#subscribe(Action)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html#subscribe(io.reactivex.functions.Action)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(action: `[`Action`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Action.html)`!, onError: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Completable#subscribe(Action, Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html#subscribe(io.reactivex.functions.Action, io.reactivex.functions.Consumer<? super java.lang.Throwable>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(observer: `[`CompletableObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableObserver.html)`!): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Proxy for ``[`Completable#subscribe(CompletableObserver)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html#subscribe(io.reactivex.CompletableObserver)).

