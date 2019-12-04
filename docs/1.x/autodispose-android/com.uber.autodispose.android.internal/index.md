[autodispose-android](../index.md) / [com.uber.autodispose.android.internal](./index.md)

## Package com.uber.autodispose.android.internal

### Types

| Name | Summary |
|---|---|
| [AutoDisposeAndroidUtil](-auto-dispose-android-util/index.md) | `open class AutoDisposeAndroidUtil`<br>`open class AutoDisposeAndroidUtil` |
| [MainThreadDisposable](-main-thread-disposable/index.md) | `abstract class MainThreadDisposable : `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)<br>`abstract class MainThreadDisposable : `[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html)<br>Copy of the MainThreadDisposable from RxAndroid which makes use of ``[` `](-auto-dispose-android-util/is-main-thread.md). This allows disposing on the JVM without crashing due to the looper check (which is often stubbed in tests). |
