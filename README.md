AutoDispose
===========

[![Build Status](https://travis-ci.org/uber/AutoDispose.svg?branch=master)](https://travis-ci.org/uber/AutoDispose)

**AutoDispose** is an RxJava 2 tool for automatically binding the execution of RxJava 2 streams to a 
provided scope via disposal/cancellation.

Overview
--------

Often (especially in mobile applications), Rx subscriptions need to stop in response to some event 
(for instance, when Activity#onStop() executes in an Android app). In order to support this common 
scenario in RxJava 2, we built AutoDispose.

The idea is simple: construct your chain like any other, and then at subscription you simply drop in
the relevant factory call + method for that type as a converter. In everyday use, it 
 usually looks like this: 

```java
myObservable
    .doStuff()
    .as(autoDisposable(this))   // The magic
    .subscribe(s -> ...);
```

By doing this, you will automatically unsubscribe from `myObservable` as indicated by your 
scope - this helps prevent many classes of errors when an observable emits and item, but the actions 
taken in the subscription are no longer valid. For instance, if a network request comes back after a
 UI has already been torn down, the UI can't be updated - this pattern prevents this type of bug.

### `autoDisposable()`

The main entry point is via static factory `autoDisposable()` methods in the `AutoDispose` class. 
There are two overloads: `Completable` and `ScopeProvider`. They return an 
`AutoDisposeConverter` object that implements all the RxJava `Converter` interfaces for use with
the `as()` operator in RxJava types.

#### Completable (as a scope)

The `Completable` semantic is modeled after the `takeUntil()` operator, which accepts an `Observable` 
whose first emission is used as a notification to signal completion. This is logically the 
behavior of a `Single`, so we choose to make that explicit. Since the type doesn't matter, we 
simplify this further to just be a `Completable`, where the scope-end emission is just a completion event.
All scopes in AutoDispose eventually resolve to a `Completable` that emits the end-of-scope notification
in `onComplete`. `onError` will pass through to the underlying subscription.

#### ScopeProvider

```java
public interface ScopeProvider {
  CompletableSource requestScope() throws Exception;
}
```

`ScopeProvider` is an abstraction that allows objects to expose and control and provide their own scopes.
This is particularly useful for objects with simple scopes ("stop when I stop") or very custom state
that requires custom handling.

Note that Exceptions can be thrown in this, and will be routed through `onError()`. If the thrown exception
is an instance of `OutsideScopeException`, it will be routed through any `OutsideScopeHandler`s (more below)
first, and sent through `onError()` if not handled.

### AutoDisposePlugins

Modeled after RxJava's plugins, this allows you to customize the behavior of AutoDispose.

#### OutsideScopeHandler

When a scope is bound to outside of its allowable boundary, `AutoDispose` will send an error event with an
 `OutsideScopeException` to downstream consumers. If you want to customize this behavior, you can use 
 `AutoDisposePlugins#setOutsideScopeHandler` to intercept these exceptions and rethrow something 
 else or nothing at all.

Example
```java
AutoDisposePlugins.setOutsideScopeHandler(t -> {
    // Swallow the exception, or rethrow it, or throw your own!
})
```

A good use case of this is, say, just silently disposing/logging observers outside of lifecycle exceptions in production but crashing on debug.

The supported mechanism to throw this is in `ScopeProvider#requestScope()` implementations.

#### FillInOutsideScopeExceptionStacktraces
 
If you have your own handling of exceptions in scope boundary events, you can optionally set
`AutoDisposePlugins#setFillInOutsideScopeExceptionStacktraces` to `false`. This will result in 
AutoDispose `not` filling in stacktraces for exceptions, for a potential minor performance boost.

### AutoDisposeAndroidPlugins

Similar to `AutoDisposePlugins`, this allows you to customize the behavior of AutoDispose in Android environments.

#### MainThreadChecker

This plugin allows for supplying a custom `BooleanSupplier` that can customize how main thread 
checks work. The conventional use case of this is Android JUnit tests, where the `Looper` class is 
not stubbed in the mock android.jar and fails explosively when touched.

Another potential use of this at runtime to customize checks for more fine-grained main thread 
checks behavior.

Example
```java
AutoDisposeAndroidPlugins.setOnCheckMainThread(() -> {
    return true; // Use whatever heuristics you prefer.
})
```

### Behavior

Under the hood, AutoDispose decorates RxJava's real observer with a custom *AutoDisposing* observer.
This custom observer leverages the scope to create a disposable, auto-disposing observer that acts 
as a lambda observer (pass-through) unless the underlying scope `CompletableSource` emits `onComplete`. Both 
scope emission and upstream termination result in immediate disposable of both the underlying scope
subscription and upstream disposable.

These custom `AutoDisposing` observers are considered public read-only API, and can be found under the 
`observers` package. They also support retrieval of the underlying observer via `delegateObserver()`
methods. Read-only API means that the public signatures will follow semantic versioning, but we may
add new methods in the future (which would break compilation if you make custom implementations!).

To read this information, you can use RxJava's `onSubscribe` hooks in `RxJavaPlugins` to watch for
instances of these observers.

### Support/Extensions

`Flowable`, `ParallelFlowable`, `Observable`, `Maybe`, `Single`, and `Completable` are all supported. Implementation is solely
based on their `Observer` types, so conceivably any type that uses those for subscription should work.

####  Extensions

There are also a number of extension artifacts available, detailed below.

##### LifecycleScopeProvider

```java
public interface LifecycleScopeProvider<E> extends ScopeProvider {
  Observable<E> lifecycle();

  Function<E, E> correspondingEvents();

  E peekLifecycle();
  
  // Inherited from ScopeProvider
  CompletableSource requestScope();
}
```

A common use case for this is objects that have implicit lifecycles, such as Android's `Activity`, 
`Fragment`, and `View` classes. Internally at subscription-time, `AutoDispose` will resolve
a `CompletableSource` representation of the target `end` event in the lifecycle, and exposes an API to dictate what
corresponding events are for the current lifecycle state (e.g. `ATTACH` -> `DETACH`). This also allows
you to enforce lifecycle boundary requirements, and by default will error if the lifecycle has either
not started yet or has already ended.

`LifecycleScopeProvider` is a special case targeted at binding to things with lifecycles. Its API is
as follows:
  - `lifecycle()` - returns an `Observable` of lifecycle events. This should be backed by a `BehaviorSubject`
  or something similar (`BehaviorRelay`, etc).
  - `correspondingEvents()` - a mapping of events to corresponding ones, i.e. Attach -> Detach.
  - `peekLifecycle()` - returns the current lifecycle state of the object.

In `requestScope()`, the implementation expects to these pieces to construct a `CompletableSource` representation 
of the proper end scope, while also doing precondition checks for lifecycle boundaries. If a 
lifecycle has not started, it will send you to `onError` with a `LifecycleNotStartedException`. If 
the lifecycle as ended, it is recommended to throw a `LifecycleEndedException` in your 
`correspondingEvents()` mapping, but it is up to the user.

To to simplify implementations, there's an included `LifecycleScopes` utility class with factories 
for generating `CompletableSource` representations from `LifecycleScopeProvider` instances.

There are three artifacts with this support:
- `autodispose-lifecycle`: Contains the core `LifecycleScopeProvider` and `LifecycleScopes` APIs. Also has a convenience test helper.
- `autodispose-lifecycle-jdk8`: Contains a simple `DefaultLifecycleScopeProvider` with a Java 8 `default`
method implementation of `requestScope()`.
- `autodispose-lifecycle-ktx`: Contains convenience kotlin extension functions and a `KotlinLifecycleScopeProvider`
that has a default implementation for `requestScope()` (similar to the jdk8 artifact, but for Kotlin).

##### Android

There are three artifacts with extra support for Android:
* `autodispose-android` has a `ViewScopeProvider` for use with Android `View` classes.
* `autodispose-android-archcomponents` has a `AndroidLifecycleScopeProvider` for use with `LifecycleOwner` and `Lifecycle` implementations.
* `autodispose-android-archcomponents-test` has a `TestLifecycleOwner` for use in testing.

For each artifact, there is a corresponding kotlin extensions artifact with it. Example:
`autodispose-android` -> `autodispose-android-ktx`.

##### Kotlin

Kotlin extension artifacts are available for almost every artifact by adding `-ktx` to the ID like 
above.

##### RxLifecycle

As of 0.4.0 there is an RxLifecycle interop module under `autodispose-rxlifecycle`. This is for interop
with [RxLifecycle](https://github.com/trello/RxLifecycle)'s `LifecycleProvider` interfaces.

### Philosophy

Each factory returns a subscribe proxies upon application that just proxy to real subscribe calls under 
the hood to "AutoDisposing" implementations of the types. These types decorate the actual observer 
at subscribe-time to achieve autodispose behavior. The types are *not* exposed directly because
autodisposing has *ordering* requirements; specifically, it has to be done at the end of a chain to properly
wrap all the upstream behavior. Lint could catch this too, but we have seen no use cases for disposing 
upstream (which can cause a lot of unexpected behavior). Thus, we optimize for the common case, and the
API is designed to prevent ordering issues while still being a drop-in one-liner.

## Motivations

Lifecycle management with RxJava and Android is nothing new, so why yet another tool?

Two common patterns for binding execution in RxJava that we used prior to this were as follows:

* `CompositeSubscription` field that all subscriptions had to be manually added to.
* `RxLifecycle`, which works via `compose()` to resolve the lifecycle end event and ultimately transform the
given observable to `takeUntil()` that event is emitted.

Both implementations are elegant and work well, but came with caveats that we sought to revisit and solve
in AutoDispose. 

`CompositeSubscription` requires manual capture of the return value of `subscribe` calls, and
gets tedious to reason about with regards to binding subscription until different events.

[`RxLifecycle`][rxlifecycle] solves the caveats of `CompositeSubscription` use by working in a dead-simple API and handling
resolution of corresponding events. It works great for `Observable` types, but due to the nature of 
how `takeUntil()` works, we found that `Single` and `Completable` usage was risky to use (particularly in a 
 large team with varying levels of RxJava experience) considering lifecycle interruption would result
in a downstream `CancellationException` every time. It's the contract of those types, but induced a lot of
ceremony for what would otherwise likely be our most commonly used type (`Single`). Even with `Observable`,
we were still burned occasionally by the completion event still coming through to an unsuspecting engineer.
Another caveat we often ran into (and later aggressively linted against) was that the `compose()` call had
ordering implications, and needed to be as close to the `subscribe()` call as possible to properly wrap upstream.
If binding to views, there were also threading requirements on the observable chain in order to work properly.
 
At the end of the day, we wanted true disposal/unsubscription-based behavior, but with RxLifecycle-esque
semantics around scope resolution. RxJava 2's `Observer` interfaces provide the perfect mechanism for
 this via their `onSubscribe()` callbacks. The result is de-risked `Single`/`Completable` usage, no ordering
 concerns, no threading concerns (fingers crossed), and true disposal with no further events of any kind
 upon scope end. We're quite happy with it, and hope the community finds it useful as well.
 
Special thanks go to [Dan Lew][dan] (creator of RxLifecycle), who helped pioneer this area for RxJava 
 in android and humored many of the discussions around lifecycle handling over the past couple years 
 that we've learned from. Much of the internal scope resolution mechanics of `AutoDispose` are 
 inspired by RxLifecycle.
 
## RxJava 1

This pattern is *sort of* possible in RxJava 1, but only on `Subscriber` (via `onStart()`) and 
`CompletableObserver` (which matches RxJava 2's API). We are aggressively migrating our internal code
 to RxJava 2, and do not plan to try to backport this to RxJava 1.

Static analysis
-------

There is an optional error-prone checker you can use to enforce use of AutoDispose.
Integration steps and more details
can be found on the [wiki](https://github.com/uber/AutoDispose/wiki/Error-Prone-Checker)

Download
--------

Java:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose)

```gradle
compile 'com.uber.autodispose:autodispose:x.y.z'
```

LifecycleScopeProvider:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-lifecycle.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-lifecycle)
```gradle
compile 'com.uber.autodispose:autodispose-lifecycle:x.y.z'
compile 'com.uber.autodispose:autodispose-lifecycle-jdk8:x.y.z'
compile 'com.uber.autodispose:autodispose-lifecycle-ktx:x.y.z'
```

Android extensions:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android)
```gradle
compile 'com.uber.autodispose:autodispose-android:x.y.z'
```

Android Architecture Components extensions:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-archcomponents.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-archcomponents)
```gradle
compile 'com.uber.autodispose:autodispose-android-archcomponents:x.y.z'
```

Android Architecture Components Test extensions:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-archcomponents-test.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-archcomponents-test)
```gradle
compile 'com.uber.autodispose:autodispose-android-archcomponents-test:x.y.z'
```

RxLifecycle interop:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-rxlifecycle.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-rxlifecycle)
```gradle
compile 'com.uber.autodispose:autodispose-rxlifecycle:x.y.z'
```

Kotlin extensions:

`autodispose-ktx` [![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-ktx.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-ktx) 
```gradle
compile 'com.uber.autodispose:autodispose-ktx:x.y.z'
```

`autodispose-android-ktx` [![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-ktx.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-ktx) 
```gradle
compile 'com.uber.autodispose:autodispose-android-ktx:x.y.z'
```

`autodispose-android-archcomponents-ktx` [![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-archcomponents-ktx.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-archcomponents-ktx) 
```gradle
compile 'com.uber.autodispose:autodispose-android-archcomponents-ktx:x.y.z'
```

`autodispose-android-archcomponents-test-ktx` [![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-archcomponents-test-ktx.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-archcomponents-test-ktx) 
```gradle
compile 'com.uber.autodispose:autodispose-android-archcomponents-test-ktx:x.y.z'
```

Javadocs and KDocs for the most recent release can be found here: https://uber.github.io/AutoDispose/0.x/

Snapshots of the development version are available in [Sonatype's snapshots repository][snapshots].

License
-------

    Copyright (C) 2017 Uber Technologies

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [rxlifecycle]: https://github.com/trello/RxLifecycle/
 [dan]: https://twitter.com/danlew42
 [snapshots]: https://oss.sonatype.org/content/repositories/snapshots/com/uber/autodispose/
