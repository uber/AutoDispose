[autodispose-android](../../index.md) / [com.uber.autodispose.android.internal](../index.md) / [MainThreadDisposable](./index.md)

# MainThreadDisposable

`abstract class MainThreadDisposable : `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)
`abstract class MainThreadDisposable : `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)

Copy of the MainThreadDisposable from RxAndroid which makes use of ``[` `](../-auto-dispose-android-util/is-main-thread.md). This allows disposing on the JVM without crashing due to the looper check (which is often stubbed in tests).

