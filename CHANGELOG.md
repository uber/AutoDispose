Changelog
=========

Version 0.3.0
----------------------------

* **New: Static factory API ([#88](https://github.com/uber/AutoDispose/pull/88))**

After a long time trying to figure out how to finagle this in a way that played nice with IDE autocomplete,
the main API for AutoDispose is now via the `AutoDispose` class and its static factories.

```java
Observable(1)
  .to(AutoDispose.with(yourScope).forObservable())
  .subscribe();

// Note: on Java 7, you must specify the generic. The IDE should autocomplete this for you.
Observable(1)
  .to(AutoDispose.with(yourScope).<Integer>forObservable())
  .subscribe();
```

`with()` has three overloads for `Maybe`, `ScopeProvider`, and `LifecycleScopeProvider`. They return
an intermediary `ScopeHandler`, which in turn has 5 generic `for___()` methods that correspond to the
5 RxJava types (`Observable`, `Flowable`, `Single`, `Maybe`, and `Completable`).

The old `Scoper` class are now **deprecated**, and will be removed in AutoDispose 1.0. Fortunately, 
this is easy to migrate via IntelliJ's structural replace. Information can be found [here](https://github.com/uber/AutoDispose/wiki/Migrating-from-0.2.0-to-0.3.0).

* **New: Support for Android Architecture Components! ([#71](https://github.com/uber/AutoDispose/pull/71))**

With the beta release of architecture components, they are now supported in the `autodispose-android-archcomponents` 
artifact. Thanks to [@yigit](https://github.com/yigit), [@jaychang0917](https://github.com/jaychang0917), and [@lsvijay](https://github.com/lsvijay) for their help and contributions on this!

```java
Observable(1)
  .to(AutoDispose.with(AndroidLifecycleScopeProvider.from(this)).forObservable())
  .subscribe();
```

Where `this` could be anything that implements `LifecycleOwner` or extends `Lifecycle`.

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-archcomponents.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-archcomponents)
```gradle
compile 'com.uber.autodispose:autodispose-android-archcomponents:x.y.z'
```

* **New: Delegate Observer retrieval ([#89](https://github.com/uber/AutoDispose/pull/89))**

Every automatically disposing observer implements one of the corresponding `AutoDisposing____Observer`
interfaces in the `com.uber.autodispose.observers` package. They are considered read-only public API,
with the intention that you can look for them in the RxJava plugin system (such as an onSubscribe hook).
This extends their functionality to expose a new experimental API called `delegateObserver()`. This allows
you to access the underlying observer that this is automatically disposing.

The reason for this is that there may be conditions where you want to handle functionality depending
on information from that Observer. As of RxJava 2.1.4, one such case could be to read information from
a `LambdaConsumerIntrospection` ([relevant PR](https://github.com/ReactiveX/RxJava/pull/5590)).

In the future, this will likely be narrowed to return a `@Nullable lambdaConsumerIntrospection()`, but 
we're open to feedback if others think this should remain the high level `Observer` type.

Thanks to [@mswysocki](https://github.com/mswysocki) for his contribution on this!

* **New: JSR 305 Nullability Annotations ([#79](https://github.com/uber/AutoDispose/pull/79))**

AutoDispose packages now leverage JSR 305 annotations to indicate nullability. By default, all methods
and parameters are non-null by default. This uses the spin-off [javax-extras](https://github.com/uber-common/javax-extras)
artifact for method nullability support, and is only a `compileOnly` dependency (so it should show up)
for tooling but is not packaged as a compile dependency.

Further reading: https://medium.com/square-corner-blog/non-null-is-the-default-58ffc0bb9111

* **New: Sample android app! ([#97](https://github.com/uber/AutoDispose/pull/97))**

For a long time, AutoDispose relied on tests and the README to be demonstrations of API use. It's become
clear that this is not enough information though, so we've added a full sample app (borrowing heavily
from RxLifecycle/Conductor's) to better illustrate API usage and runtime behavior. We'll continue to
iterate on this over time.

* **Improved: EndConsumerHelper ([#77](https://github.com/uber/AutoDispose/pull/77))**

AutoDispose uses the same disposal-helper utilities as RxJava. This updates to RxJava's new 
`EndConsumerHelper`, which should hopefully help produce more helpful error messages in disposal error
conditions.

* **Other**

Updated various dependencies:
  
    Android Arch Components: 1.0.0-beta1
    Android Support Library: 26.1.0 (to match arch components)
    Kotlin: 1.1.50 
    
As always, we welcome any and all discussions/feedback/PRs! We're marching toward a 1.0 release Real 
Soon Now, so now is the time. There are a few outstanding discussion issues in the issue tracker about 
1.0 final design decisions.

Version 0.2.0
----------------------------

* **New: Kotlin artifact! (#47)**

This adds `autoDisposeWith()` extensions to RxJava types.

```kotlin
myObservable
   .doWhatever()
   .autoDisposeWith(this)
   .subscribe()
```

* **New: Plugin system! (#57)**

Modeled after RxJava's plugins, this allows you to customize the behavior of AutoDispose with lifecycle boundary checks.

```java
AutoDisposePlugins.setOutsideLifecycleHandler(t -> {
    // Swallow the exception, or rethrow it, or throw your own!
})
```

A good use case of this is, say, just silently disposing/logging observers outside of lifecycle exceptions in production but crashing on debug.

* **New: Test helpers! (#48 #49)**

Two helpers were added to simulate conditions in testing.
- `TestLifecycleScopeProvider`
  - This has two corresponding lifecycle methods: `start()` and `stop()`
- `TestScopeProvider`
  - Has just one method - `emit()`.

For testing with just the `Maybe<?>` scope, we recommend using RxJava's built-in `MaybeSubject`.

* **Fix**: Fixed a race condition where upstream wouldn't be disposed if the lifecycle emitted or error'd synchronously (i.e. was already terminated). (#57)
* **Fix**: Add missing `@CheckReturnValue` annotations to `subscribeWith` methods. (#53)

**Other tidbits:**
- Removed `@NonNull` annotations. Everything is `@NonNull` by default, and only elements 
annotated with `@Nullable` are not.
- Use of the new `java-library` plugin for gradle (#64). The RxJava dependencies are marked as `api`.
- Error prone has been integrated. Currently the annotations are just marked as `compileOnly`, but if a need arises/community wants them - we can compile them in a future version.

Version 0.1.0
----------------------------

* Initial release
