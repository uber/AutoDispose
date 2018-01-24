Changelog
=========

Version 0.5.1
----------------------------

_2017-12-6_

**Fix:** A bug where unbound scopes would mark the observer as disposed, subsequently preventing future events from emitting. ([#149](https://github.com/uber/autodispose/issues/149))

**Fix:** Kotlin docs accidentally pointing to `to()` operators instead of `as()`. ([#145](https://github.com/uber/autodispose/issues/145))

Snapshots should be fully working now as well.

Version 0.5.0
----------------------------

_2017-12-3_

### New converter-based API for use with as() ([#141](https://github.com/uber/autodispose/issues/141))

AutoDispose's primary API is now via static `autoDisposable()` methods on the `AutoDispose` class. The previous `to()` based APIs are now completely deprecated, and will be removed in AutoDispose 1.0.

This has been sort of the long-standing ideal API for AutoDispose for awhile, but wasn't possible until the introduction of the new `as()` operator in RxJava. As this operator is still not marked as stable (and won't until RxJava 2.2.0), AutoDispose will not be updated to 1.0 until then.

The main difference is that you no longer have to specify the type indirection, and the returned converter is applicable for all 5 RxJava types. In use, it looks like this:

```java
Flowable.just(1)
    .as(autoDisposable(scope))
    .subscribe()

Observable.just(1)
    .as(autoDisposable(scope))
    .subscribe()

Maybe.just(1)
    .as(autoDisposable(scope))
    .subscribe()

Single.just(1)
    .as(autoDisposable(scope))
    .subscribe()

Completable.complete()
    .as(autoDisposable(scope))
    .subscribe()
```

There are three overloads for `autoDisposable()`, for each of the three scope types (`Maybe`, `ScopeProvider`, and `LifecycleScopeProvider`).

The Kotlin bindings have also been updated to match semantics, with the `autoDisposeWith` extension functions being deprecated in favor of analogous `autoDisposable`. These are `WARNING` level in this release, and will become `ERROR` in AutoDispose 0.6.0, before finally being removed in 1.0. They also provide `replaceWith` options (compatible with Kotlin's deprecation quickfixes).

`autoDisposable` reads best when statically imported (so you can do `.as(autoDisposable(...))`, which you can safely do if you're using Java 8.

For structural replace templates, see [here](https://github.com/uber/AutoDispose/wiki/Migrating-from-0.4.0-to-0.5.0)).

### Fixed a lot of concurrency edge cases and performance improvements after review from David Karnok ([#138](https://github.com/uber/autodispose/issues/138) and [#130](https://github.com/uber/autodispose/issues/130))

David Karnok (@akarnokd, RxJava project lead) did an audit of the current codebase and gave extensive feedback in #130. #138 implements that feedback. This handled a lot of concurrency gotchas and edge cases we were missing before. See the issue and PR for full details.

### Plugin for controlling whether or not to fill in stacktraces ([#124](https://github.com/uber/autodispose/issues/124))

`AutoDisposePlugins` has a new API to control whether or not lifecycle exception stacktraces are filled in. What this means is that if you opt out, the exceptions thrown in `LifecycleScopeProvider` boundary issues will no longer have a stacktrace (`getStacktrace()` will return an empty array) and only carry the type name and message. This can be useful to gain some performance if you track stacktracing via other means.

### UNBOUND shorthand ([#125](https://github.com/uber/autodispose/issues/125))

`ScopeProvider` has a static instance of an "unbound" provider directly in the interface now for reuse. This obviates the need for `TestScopeProvider#unbound()`, which has been **removed**. Usage is simple:

```java
Observable.just(1)
    .as(autoDisposable(ScopeProvider.UNBOUND))
    .subscribe()
```

## Misc

- Archcomponents updated to 1.0.0 final ([#128](https://github.com/uber/autodispose/issues/128))
- RxJava dependency is now 2.1.7 (to leverage `as()`) ([#141](https://github.com/uber/autodispose/issues/141))
- Kotlin is now updated to 1.2.0 ([#141](https://github.com/uber/autodispose/issues/141))
- Dokka is wired up, meaning that kotlin artifacts now also have exported javadocs. ([#126](https://github.com/uber/autodispose/issues/126))
- `subscribeBy` example extension in the sample app displaying how you can add extension functions to the `*SubscribeProxy` classes. ([#127](https://github.com/uber/autodispose/issues/127))
- `delegateObserver()` APIs on `AutoDisposing` observers have been promoted to stable. Considering they are useful for `subscribeWith()`, we can just keep it observer-based and keep the library more flexible long-term ([#144](https://github.com/uber/autodispose/issues/144))

Thanks to the following contributors! [@charlesdurham](https://github.com/@charlesdurham) [@ajalt](https://github.com/@ajalt) [@tbsandee](https://github.com/@tbsandee) [@akarnokd](https://github.com/@akarnokd)

Version 0.4.0
----------------------------

_2017-10-22_

#### Structured Android Components [#111](https://github.com/uber/AutoDispose/pull/111)

Android components have been split up into several artifacts under `:android`:
-  `autodispose-android`: Core android utilities, previously `:autodispose-android`
- `autodispose-android-archcomponents`: Utilities for lifecycles in android archcomponents, previously `:autodispose-android-archcomponents` but does not have the test helper
- **New:** `autodispose-android-archcomponents-test`: Test utilities for working with arch components, namely `TestLifecycleOwner`, formerly `TestAndroidLifecycleScopeProvider`.
  - This allows us to remove the `extensions` dependency from the main arch components artifact and keep this optional. This API can also be used for general use testing for arch components, as it's not actually specific to AutoDispose.
- **New:** `autodispose-android-kotlin`: kotlin bindings for `autodispose-android`
- **New:** `autodispose-android-archcomponents-kotlin`: kotlin bindings for `autodispose-android-archcomponents`
- **New:** `autodispose-android-archcomponents-test-kotlin`: kotlin bindings for `autodispose-android-test-archcomponents`
- **New:** Android artifacts include consumer proguard rules (relates to ([#112](https://github.com/uber/AutoDispose/issues/112)))

Related changes:
- **Fix:** Arch components updated to `1.0.0-rc1`, which should fix compatibility issues noted in ([#113](https://github.com/uber/AutoDispose/issues/113))
- **Enhancement:** `untilEvent` overload for AndroidLifecycleScopeProvider ([#107](https://github.com/uber/AutoDispose/issues/107))
  - Now you can bind until a specific target event, or provide your own boundary provider function
- Behavior change: previously, anything occurring after `ON_STOP` would resolve to `ON_DESTROY`. Now, they resolve to stop on the next destruction event. This brings it inline with the modern behavior of arch components version `-rc1`.
- **Enhancement:** `AndroidLifecycleScopeProvider`s are now reusable. This is somewhat experimental, as it works by dynamically resolving the last event based on the state. Please report any issues! [#121](https://github.com/uber/AutoDispose/pull/121)

#### RxLifecycle Interop

A new `autodispose-rxlifecycle` interop module was added, adding support for scoping to [RxLifecycle](https://github.com/trello/RxLifecycle)'s `LifecycleProvider` API. ([#118](https://github.com/uber/AutoDispose/pull/118))

#### Misc

- Reduced object allocations ([#108](https://github.com/uber/AutoDispose/issues/108))
- Convenience `unbound()` factory on `TestScopeProvider` ([#108](https://github.com/uber/AutoDispose/issues/108))
- Removed synthetic accessors ([#103](https://github.com/uber/AutoDispose/issues/103))
- Updated to Kotlin 1.1.51 ([#116](https://github.com/uber/AutoDispose/issues/116))

Thanks to the following contributors! [@rubengees](https://github.com/rubengees) [@bangarharshit](https://github.com/bangarharshit) 

#### Updated dependencies:
```
Android Arch Components: 1.0.0-rc1
Android Arch Components (common): 1.0.3
Kotlin: 1.1.51
```

#### New artifacts coordinates:

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-archcomponents-test.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-archcomponents-test)
```gradle
compile 'com.uber.autodispose:autodispose-android-archcomponents-test:x.y.z'
```

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-rxlifecycle.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-rxlifecycle)
```gradle
compile 'com.uber.autodispose:autodispose-rxlifecycle:x.y.z'
```

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-kotlin.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-kotlin) 
```gradle
compile 'com.uber.autodispose:autodispose-android-kotlin:x.y.z'
```

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-archcomponents-kotlin.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-archcomponents-kotlin) 
```gradle
compile 'com.uber.autodispose:autodispose-android-archcomponents-kotlin:x.y.z'
```

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.autodispose/autodispose-android-archcomponents-test-kotlin.svg)](https://mvnrepository.com/artifact/com.uber.autodispose/autodispose-android-archcomponents-test-kotlin) 
```gradle
compile 'com.uber.autodispose:autodispose-android-archcomponents-test-kotlin:x.y.z'
```

Version 0.3.0
----------------------------

_2017-10-01_

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
artifact.

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

Thanks to [@yigit](https://github.com/yigit), [@jaychang0917](https://github.com/jaychang0917), and [@lsvijay](https://github.com/lsvijay) for their help and contributions on this!

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

_2017-05-08_

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

_2017-03-13_

* Initial release
