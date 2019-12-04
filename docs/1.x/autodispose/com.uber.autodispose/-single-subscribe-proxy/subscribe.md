[autodispose](../../index.md) / [com.uber.autodispose](../index.md) / [SingleSubscribeProxy](index.md) / [subscribe](./subscribe.md)

# subscribe

`abstract fun subscribe(): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Single#subscribe()`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html#subscribe()).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onSuccess: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Single#subscribe(Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html#subscribe(io.reactivex.functions.Consumer<? super T>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(biConsumer: `[`BiConsumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BiConsumer.html)`<in `[`T`](index.md#T)`, in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Single#subscribe(BiConsumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html#subscribe(io.reactivex.functions.BiConsumer<? super T,? super java.lang.Throwable>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(onSuccess: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`T`](index.md#T)`>!, onError: `[`Consumer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html)`<in `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`!>!): `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)`!`

Proxy for ``[`Single#subscribe(Consumer, Consumer)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html#subscribe(io.reactivex.functions.Consumer<? super T>, io.reactivex.functions.Consumer<? super java.lang.Throwable>)).

**Return**
[Disposable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)!: a ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

`abstract fun subscribe(observer: `[`SingleObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleObserver.html)`<in `[`T`](index.md#T)`>!): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Proxy for ``[`Single#subscribe(SingleObserver)`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html#subscribe(io.reactivex.SingleObserver<? super T>)).

