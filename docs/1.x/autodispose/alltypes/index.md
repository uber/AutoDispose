

AutoDispose is an RxJava 2 tool for automatically binding the execution of RxJava 2 streams to a
provided scope via disposal/cancellation.

### All Types

| Name | Summary |
|---|---|
| [com.uber.autodispose.AutoDispose](../com.uber.autodispose/-auto-dispose/index.md) | Factories for autodispose converters that can be used with RxJava types' corresponding `as(...)` methods to transform them into auto-disposing streams.  |
| [com.uber.autodispose.AutoDisposeContext](../com.uber.autodispose/-auto-dispose-context/index.md) | A context intended for use as `AutoDisposeContext.() -> Unit` function body parameters where zero-arg [autoDispose](../com.uber.autodispose/-auto-dispose-context/auto-dispose.md) functions can be called. This should be backed by an underlying [Completable](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) or [ScopeProvider](../com.uber.autodispose/-scope-provider/index.md). |
| [com.uber.autodispose.AutoDisposeConverter](../com.uber.autodispose/-auto-dispose-converter.md) | A custom converter that implements all the RxJava types converters, for use with the `as()` operator. |
| [com.uber.autodispose.AutoDisposePlugins](../com.uber.autodispose/-auto-dispose-plugins/index.md) | Utility class to inject handlers to certain standard autodispose-lifecycle operations. |
| [com.uber.autodispose.observers.AutoDisposingCompletableObserver](../com.uber.autodispose.observers/-auto-disposing-completable-observer/index.md) | A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`CompletableObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableObserver.html) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation. |
| [com.uber.autodispose.observers.AutoDisposingMaybeObserver](../com.uber.autodispose.observers/-auto-disposing-maybe-observer/index.md) | A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`MaybeObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/MaybeObserver.html) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation. |
| [com.uber.autodispose.observers.AutoDisposingObserver](../com.uber.autodispose.observers/-auto-disposing-observer/index.md) | A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`Observer`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observer.html) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation. |
| [com.uber.autodispose.observers.AutoDisposingSingleObserver](../com.uber.autodispose.observers/-auto-disposing-single-observer/index.md) | A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`SingleObserver`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleObserver.html) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation. |
| [com.uber.autodispose.observers.AutoDisposingSubscriber](../com.uber.autodispose.observers/-auto-disposing-subscriber/index.md) | A ``[`Disposable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html) ``[`Subscriber`](#) that can automatically dispose itself. Interface here for type safety but enforcement is left to the implementation. |
| [io.reactivex.Completable](../com.uber.autodispose/io.reactivex.-completable/index.md) (extensions in package com.uber.autodispose) |  |
| [com.uber.autodispose.CompletableSubscribeProxy](../com.uber.autodispose/-completable-subscribe-proxy/index.md) | Subscribe proxy that matches ``[`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html)'s subscribe overloads. |
| [com.uber.autodispose.internal.DoNotMock](../com.uber.autodispose.internal/-do-not-mock/index.md) | This annotation indicates that a given type should not be mocked. This is a copy of what was in Error-Prone's annotations artifact before it was removed, but left for documentation purposes.  |
| [io.reactivex.Flowable](../com.uber.autodispose/io.reactivex.-flowable/index.md) (extensions in package com.uber.autodispose) |  |
| [com.uber.autodispose.FlowableSubscribeProxy](../com.uber.autodispose/-flowable-subscribe-proxy/index.md) | Subscribe proxy that matches ``[`Flowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html)'s subscribe overloads. |
| [io.reactivex.Maybe](../com.uber.autodispose/io.reactivex.-maybe/index.md) (extensions in package com.uber.autodispose) |  |
| [com.uber.autodispose.MaybeSubscribeProxy](../com.uber.autodispose/-maybe-subscribe-proxy/index.md) | Subscribe proxy that matches ``[`Maybe`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Maybe.html)'s subscribe overloads. |
| [io.reactivex.Observable](../com.uber.autodispose/io.reactivex.-observable/index.md) (extensions in package com.uber.autodispose) |  |
| [com.uber.autodispose.ObservableSubscribeProxy](../com.uber.autodispose/-observable-subscribe-proxy/index.md) | Subscribe proxy that matches ``[`Observable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observable.html)'s subscribe overloads. |
| [com.uber.autodispose.OutsideScopeException](../com.uber.autodispose/-outside-scope-exception/index.md) | Signifies an error occurred due to execution starting outside the lifecycle. |
| [io.reactivex.parallel.ParallelFlowable](../com.uber.autodispose/io.reactivex.parallel.-parallel-flowable/index.md) (extensions in package com.uber.autodispose) |  |
| [com.uber.autodispose.ParallelFlowableSubscribeProxy](../com.uber.autodispose/-parallel-flowable-subscribe-proxy/index.md) | Subscribe proxy that matches ``[`ParallelFlowable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/parallel/ParallelFlowable.html)'s subscribe overloads. |
| [com.uber.autodispose.ScopeProvider](../com.uber.autodispose/-scope-provider/index.md) | Provides a ``[`CompletableSource`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableSource.html) representation of a scope. The emission of this is the signal |
| [com.uber.autodispose.Scopes](../com.uber.autodispose/-scopes/index.md) | Utilities for dealing with AutoDispose scopes. |
| [io.reactivex.Single](../com.uber.autodispose/io.reactivex.-single/index.md) (extensions in package com.uber.autodispose) |  |
| [com.uber.autodispose.SingleSubscribeProxy](../com.uber.autodispose/-single-subscribe-proxy/index.md) | Subscribe proxy that matches ``[`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html)'s subscribe overloads. |
| [com.uber.autodispose.TestScopeProvider](../com.uber.autodispose/-test-scope-provider/index.md) | ScopeProvider implementation for testing. You can either back it with your own instance, or just stub it in place and use its public emit APIs. |
