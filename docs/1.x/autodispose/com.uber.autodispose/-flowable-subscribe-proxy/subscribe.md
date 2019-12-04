[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [FlowableSubscribeProxy](index.md) / [subscribe](./subscribe.md)

# subscribe

`abstract fun subscribe(): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Flowable#subscribe()`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#subscribe()).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onNext: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Flowable#subscribe(Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#subscribe(io.reactivex.functions.Consumer<? super T>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onNext: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!, onError: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Flowable#subscribe(Consumer, Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#subscribe(io.reactivex.functions.Consumer<? super T>, io.reactivex.functions.Consumer<? super java.lang.Throwable>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onNext: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!, onError: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!, onComplete: `[`Action`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Action.html)`!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Flowable#subscribe(Consumer, Consumer, Action)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#subscribe(io.reactivex.functions.Consumer<? super T>, io.reactivex.functions.Consumer<? super java.lang.Throwable>, io.reactivex.functions.Action)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onNext: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!, onError: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!, onComplete: `[`Action`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Action.html)`!, onSubscribe: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in Subscription!>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Flowable#subscribe(Consumer, Consumer, Action, Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#subscribe(io.reactivex.functions.Consumer<? super T>, io.reactivex.functions.Consumer<? super java.lang.Throwable>, io.reactivex.functions.Action, io.reactivex.functions.Consumer<? super org.reactivestreams.Subscription>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(observer: Subscriber<in `[`T`](index.md#T)`>!): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Proxy for ``[`Flowable#subscribe(Subscriber)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#subscribe(org.reactivestreams.Subscriber<? super T>)).

