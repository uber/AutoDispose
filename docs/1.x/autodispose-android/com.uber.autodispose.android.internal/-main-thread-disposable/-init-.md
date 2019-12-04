[autodispose-android](../../index.md) / [com.uber.autodispose.android.internal](../index.md) / [MainThreadDisposable](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`MainThreadDisposable()`
`MainThreadDisposable()`

Copy of the MainThreadDisposable from RxAndroid which makes use of ``[` `](../-auto-dispose-android-util/is-main-thread.md). This allows disposing on the JVM without crashing due to the looper check (which is often stubbed in tests).

