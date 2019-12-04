[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [ObservableSubscribeProxy](index.md) / [subscribe](./subscribe.md)

# subscribe

`abstract fun subscribe(): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Observable#subscribe()`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#subscribe()).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onNext: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Observable#subscribe(Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#subscribe(io.reactivex.functions.Consumer<? super T>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onNext: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!, onError: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Observable#subscribe(Consumer, Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#subscribe(io.reactivex.functions.Consumer<? super T>, io.reactivex.functions.Consumer<? super java.lang.Throwable>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onNext: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!, onError: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!, onComplete: `[`Action`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Action.html)`!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Observable#subscribe(Consumer, Consumer, Action)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#subscribe(io.reactivex.functions.Consumer<? super T>, io.reactivex.functions.Consumer<? super java.lang.Throwable>, io.reactivex.functions.Action)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onNext: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!, onError: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!, onComplete: `[`Action`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Action.html)`!, onSubscribe: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Observable#subscribe(Consumer, Consumer, Action, Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#subscribe(io.reactivex.functions.Consumer<? super T>, io.reactivex.functions.Consumer<? super java.lang.Throwable>, io.reactivex.functions.Action, io.reactivex.functions.Consumer<? super io.reactivex.disposables.Disposable>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(observer: `[`Observer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observer.html)`<in `[`T`](index.md#T)`>!): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Proxy for ``[`Observable#subscribe(Observer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html#subscribe(io.reactivex.Observer<? super T>)).

