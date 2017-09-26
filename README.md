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
    .to(AutoDispose.with(this).forObservable())   // The magic
    .subscribe(s -> ...);
```

By doing this, you will automatically unsubscribe from `myObservable` as indicated by your 
scope - this helps prevent many classes of errors when an observable emits and item, but the actions 
taken in the subscription are no longer valid. For instance, if a network request comes back after a
 UI has already been torn down, the UI can't be updated - this pattern prevents this type of bug.

### `with()` + `<type>()`

The main entry point is via static factory `with()` methods in the `AutoDispose` class. There are 
three overloads: `Maybe`, `ScopeProvider`, and `LifecycleScopeProvider`. They return a 
`ScopeHandler` object that's just intended to be an intermediary to route to the desired type 
function (this is for better autocomplete in IDEs for the generics). 

For the relevant `type` methods, there is one per RxJava type (`forObservable()`, `forSingle()`, etc). 
These return implementations of a converter `Function`, intended for use with the `to()` operator in RxJava
types.

These work in tandem to create the regular AutoDispose flow. `AutoDispose.with(scope).forObservable()`.

#### Maybe 

The `Maybe` semantic is modeled after the `takeUntil()` operator, which accepts an `Observable` 
whose first emission is used as a notification to signal completion. This is is logically the 
behavior of a `Maybe`, so we choose to make that explicit. All scopes eventually resolve to a single
`Maybe` that emits the end-of-scope notification.

#### Providers

The provider options allow you to pass in an interface of something can provide a resolvable scope. 
A common use case for this is objects that have implicit lifecycles, such as Android's `Activity`, 
`Fragment`, and `View` classes. Internally at subscription-time, `AutoDispose` will resolve
a `Maybe` representation of the target `end` event in the lifecycle, and exposes an API to dictate what
corresponding events are for the current lifecycle state (e.g. `ATTACH` -> `DETACH`). This also allows
you to enforce lifecycle boundary requirements, and by default will error if the lifecycle has either
not started yet or has already ended.

##### LifecycleScopeProvider

```java
public interface LifecycleScopeProvider<E> {
  Observable<E> lifecycle();

  Function<E, E> correspondingEvents();

  E peekLifecycle();
}
```

`LifecycleScopeProvider` is a special case targeted at binding to things with lifecycles. Its API is
as follows:
  - `lifecycle()` - returns an `Observable` of lifecycle events. This should be backed by a `BehaviorSubject`
  or something similar (`BehaviorRelay`, etc).
  - `correspondingEvents()` - a mapping of events to corresponding ones, i.e. Attach -> Detach.
  - `peekLifecycle()` - returns the current lifecycle state of the object.

`AutoDispose` uses these pieces to construct a `Maybe` representation of the proper end scope, while
also doing precondition checks for lifecycle boundaries. If a lifecycle has not started, it will send 
you to `onError` with a `LifecycleNotStartedException`. If the lifecycle as ended, it is recommended to
throw a `LifecycleEndedException` in your `correspondingEvents()` mapping, but it is up to the user.

##### ScopeProvider

```java
public interface ScopeProvider {
  Maybe<?> requestScope();
}
```

`ScopeProvider` is an abstraction that allows objects to expose and control and provide their own scopes.
This is particularly useful for objects with simple scopes ("stop when I stop") or very custom state
that requires custom handling.

#### Plugins

When a lifecycle has not started or has already ended, `AutoDispose` will send an error event with an
 `OutsideLifecycleException` to downstream consumers. If you want to customize this behavior, you can use 
 `AutoDisposePlugins` to intercept these exceptions and rethrow something else or nothing at all.

### Behavior

The created observer encapsulates the parameters of `around` to create a disposable, auto-disposing
observer that acts as a lambda observer (pass-through) unless the underlying scope `Maybe` emits.
Both scope end and upstream termination result in immediate disposable of both the underlying scope
subscription and upstream disposable.

### Support

`Flowable`, `Observable`, `Maybe`, `Single`, and `Completable` are all supported. Implementation is solely
based on their `Observer` types, so conceivably any type that uses those for subscription should work.

There is a separate Android artifact with extra APIs for Android components, such as support for `View`
lifecycle binding.

### Philosophy

Each factory returns a subscribe proxy upon application that just proxy to real subscribe calls under 
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

Download
--------

Java:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose)

```gradle
compile 'com.uber.autodispose:autodispose:x.y.z'
```

Android extensions:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android)
```gradle
compile 'com.uber.autodispose:autodispose-android:x.y.z'
```

Kotlin extensions:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-kotlin.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-kotlin)
```gradle
compile 'com.uber.autodispose:autodispose-kotlin:x.y.z'
```

Snapshots of the development version will eventually be available in [Sonatype's snapshots repository][snapshots].

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
 [snapshots]: https://oss.sonatype.org/content/repositories/snapshots/
