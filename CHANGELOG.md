Changelog
=========

Version 2.0.0
-------------

_2020-02-23_

AutoDispose 2 is built against RxJava 3 and is binary-compatible with AutoDispose 1.x and RxJava 2.x. As such - it has a different package name and maven group ID.

### Packaging

|  | Maven Group ID | Package Name |
| --- | --- | --- |
| 1.x | `com.uber.autodispose` | `com.uber.autodispose` |
| 2.x | `com.uber.autodispose2` | `autodispose2` |

For any sub-packages, the above mapping should be used for those package prefix replacements as well.

### Changes

-   All deprecated APIs in 1.x have been removed. This consisted exclusively of deprecated Kotlin `autoDisposable` extension functions that were deprecated in 1.4.0.
-   The `autodispose-android-archcomponents*` artifacts have been renamed to to `autodispose-androidx-lifecycle*` to match the `androidx-lifecycle` library they correspond to.
-   The lint and error prone checks have also been updated.
-   At the time of writing, there is no RxLifecycle with RxJava 3 support yet, and as such there is no AutoDispose 2.x interop artifact for RxLifecycle. We can add this back if there's a new RxLifecycle release with RxJava 3.x support.

You can find migration steps on the project site at https://uber.github.io/AutoDispose/migrating-1x-2x/.

Version 2.0.0-RC2
-----------------

_2019-12-02_

RC2 has no additional changes from RC1 but does not upload RCs for Android artifacts since RxAndroid does not have a RC yet.

Version 2.0.0-RC1
-----------------

_2019-12-01_

This is the first release candidate for AutoDispose 2.0

AutoDispose 2 is built against RxJava 3 and is binary-compatible with AutoDispose 1.x and RxJava 2.x. As such - it has a different package name and maven group ID.

### Android not supported yet

Due to there being no RxAndroid 3.x release candidate, we cannot release RCs of the Android artifacts yet. They are available as snapshots though.

### Packaging

|  | Maven Group ID | Package Name |
| --- | --- | --- |
| 1.x | `com.uber.autodispose` | `com.uber.autodispose` |
| 2.x | `com.uber.autodispose2` | `autodispose2` |

For any sub-packages, the above mapping should be used for those package prefix replacements as well.

### Changes

-   All deprecated APIs in 1.x have been removed. This consisted exclusively of deprecated Kotlin `autoDisposable` extension functions that were deprecated in 1.4.0.
-   The `autodispose-android-archcomponents*` artifacts have been renamed to to `autodispose-androidx-lifecycle*` to match the `androidx-lifecycle` library they correspond to.
-   The lint and error prone checks have also been updated. We may look at consolidating these before 2.0 final is released if the community wants.
-   At the time of writing, there is no RxLifecycle with RxJava 3 support yet, and as such there is no AutoDispose 2.x interop artifact for RxLifecycle. We can add this back if there's a new RxLifecycle release with RxJava 3.x support.

We'll be maintaining a running document of migration steps on the project site at https://uber.github.io/AutoDispose/migrating-1x-2x/.

Version 1.4.0
-------------

_2019-09-18_

### Kotlin `CoroutineScope` interop [#374](https://github.com/uber/AutoDispose/pull/374)

Interop functions for `CoroutineScope` to `ScopeProvider`/`Completable` (and vice versa) are now available
in a new `autodispose-coroutines-interop` artifact. This is not intended to allow AutoDispose's 
scoping machinery be a competitor for it, but rather just a tool for interop-ing codebases that use 
both or aid in migrations.

### Kotlin API naming improvements [#372](https://github.com/uber/AutoDispose/pull/372) [#377](https://github.com/uber/AutoDispose/pull/377)

To better differentiate between the `AutoDispose` classes's `autoDisposable()` methods (which return 
Rx converter types), the Kotlin `autoDisposable()` extensions have been deprecated in favor of a more 
idiomatic `autoDispose()` verb form name. The old extensions have been annotated with `@Deprecated` 
and replacements, so this should be an easy one time migration.

```kotlin
myObservable
    .autoDispose(scope)
    .subscribe()
```

### Fused proxy types [#376](https://github.com/uber/AutoDispose/pull/376)

AutoDispose's analogous Rx types (`AutoDisposeObservable`, etc) have been updated to implement 
their `*SubscribeProxy` interfaces directly. The current behavior of subscribe proxies is best 
thought of as similar to `hide()`. Subscribe proxies have always wrapped the AutoDispose's 
analogous Rx types to prevent upcasting. If your team are good citizens though, you can now disable 
proxy hiding via `AutoDisposePlugins` to save that extra allocation.

### New `withScope()` API [#375](https://github.com/uber/AutoDispose/pull/375) [#378](https://github.com/uber/AutoDispose/pull/378)

There is a new Kotlin `withScope()` API that accepts a scope and a body to execute in. This body
is a function-with-receiver tied to a new `AutoDisposeContext` interface, which has no-arg `autoDispose()`
functions in it. This allows for calling `autoDispose()` within the context of the body and allow the
enclosing context manage wiring the applied scope to it.

```kotlin
@Test
fun example() = withScope(scope) {
  Observable.just("Hello withScope()!")
      .autoDispose()
      .subscribe()
}
```

### Dependency updates

```
Kotlin: 1.3.50
Lint tools: 26.5.0
```

Version 1.3.0
-------------

_2019-05-15_

### Unified Kotlin extensions

Starting with 1.3.0, all the `-ktx` artifacts and their kotlin extensions have been merged into the main artifacts they extended. This means that extensions in an artifact like `autodispose-android-ktx` are now available directly in the corresponding `autodispose-android`. 

This is a binary-compatible change because the extensions file name has changed while the extensions themselves have remained in the same package. So in essence, `import com.uber.autodispose.autoDisposable` still works as-is. Just remove the ktx artifact dependencies and everything will still link as-is!

The Kotlin standard library has been added as an `compileOnly` dependency of artifacts containing Kotlin bindings. This is to avoid imposing the dependency for non-Kotlin users, but the expectation is for Kotlin users to bring their own standard library dependency to fulfill this if used. Considering the standard library is an ubiquitous dependency for Kotlin projects, we don't expect this to be an issue and drew inspiration for this design from [Retrofit](https://github.com/square/retrofit).

Proguard/R8 `.pro` files in the unified artifacts have been updated to not warn on these `KotlinExtensions` files as they can be safely stripped in builds if unused.

**NOTE:** One important thing this revealed was that the ktx artifacts were built with jdk target 1.6, while depending on Java artifacts that were built against JDK 8. Now that they are unified, this means that the Kotlin extensions require targeting JDK 1.8 as well (configurable via compiler arg `-jvm-target=1.8`).

PRs: [#339](https://github.com/uber/AutoDispose/pull/339) [#341](https://github.com/uber/AutoDispose/pull/341) [#346](https://github.com/uber/AutoDispose/pull/346)

### More Kotlin extensions!

Initially, we only provided minimal Kotlin extensions to support scopes on extra types like Android's `LifecycleOwner`, `View`, etc. This resulted in a bit of ceremony for these APIs to be used though, such as:

```kotlin
Observable.just(1)
    .autoDisposable(AndroidLifecycleScopeProvider.from(this))
    .subscribe()
```

To simplify this, we've added the following top-level extension functions for the following:
* `autodispose-android` - `View`
* `autodispose-archcomponents` - `LifecycleOwner`
* `autodispose-rxlifecycle` - `LifecycleProvider`
* `autodispose-rxlifecycle3` - `LifecycleProvider`

So now, the above snippet could just be:

```kotlin
Observable.just(1)
    .autoDisposable(this)
    .subscribe()
```

PRs: [#348](https://github.com/uber/AutoDispose/pull/348) [#353](https://github.com/uber/AutoDispose/pull/353)

### Removed deprecated lifecycle artifacts

Following their deprecating in 1.1.0, the `autodispose-lifecycle-jdk8` and `autodispose-lifecycle-ktx` artifacts are no longer published. Please move to just using `LifecycleScopeProvider` directly.

### Misc

Dependency updates

    Kotlin: 1.3.31
    AndroidX Annotations: 1.0.2
    RxJava: 2.2.8
    RxAndroid: 2.1.1

Artifact changes

| Original | Merged into (if applicable) |
|-|-|
| autodispose-ktx | autodispose |
| autodispose-android-ktx | autodispose-android |
| autodispose-android-archcomponents-ktx | autodispose-android-archcomponents |
| autodispose-android-archcomponents-test-ktx | autodispose-android-archcomponents-test |
| autodispose-lifecycle-ktx | N/A |
| autodispose-lifecycle-jdk8 | N/A |

_Note:_ This does not mean the existing versions were deleted or removed in any way, just that we will not publish 1.3.0 or later versions of them.

Version 1.2.0
-------------

_2019-04-03_

* Fixes a bug which allows Lint to refer to the app level `gradle.properties` file for configuration support. [#335](https://github.com/uber/AutoDispose/pull/335)
* New `autodispose-rxlifeycle3` artifact for interop with RxLifecycle3. [#319](https://github.com/uber/AutoDispose/pull/319)
* Various dependency updates
```
Lint: 26.3.2
Kotlin 1.3.21
```

All PRs: [#319](https://github.com/uber/AutoDispose/pull/319), [#320](https://github.com/uber/AutoDispose/pull/320), [#322](https://github.com/uber/AutoDispose/pull/322), [#326](https://github.com/uber/AutoDispose/pull/326), [#327](https://github.com/uber/AutoDispose/pull/327) [#328](https://github.com/uber/AutoDispose/pull/328), [#329](https://github.com/uber/AutoDispose/pull/329), [#330](https://github.com/uber/AutoDispose/pull/330), [#334](https://github.com/uber/AutoDispose/pull/334), [#335](https://github.com/uber/AutoDispose/pull/335), [#336](https://github.com/uber/AutoDispose/pull/336)

Version 1.1.0
-------------

_2018-12-13_

### Static Analysis

This is a big static analysis release. AutoDispose now ships with two static
analysis artifacts: `autodispose-lint` for Android Lint and `autodispose-error-prone` for Error-Prone.

Both of these checks operate by detecting uses of standard RxJava `subscribe`/`subscribeWith`
calls in the context of something that has scope (such as a `ScopeProvider`). If they’re
detected, the lint/checker will mark them as missing `Disposable` handling and
suggest either using AutoDispose or (if `lenient` mode enabled) manually handle
the returned `Disposable`.

Both checks have configuration support:

* `TypesWithScope` - a comma-separated list of custom types with scope. By default, this is additive to default scopes.
* `OverrideScopes` - a boolean flag indicating if `TypesWithScope` should override the built-in scopes. `false` by default.
* `Lenient` - a boolean flag to enable a lenient mode that tells the linter to ignore cases where the returned `Disposable` is captured (aka “I know what I’m doing” mode). `false` by default.

Both checkers should have feature parity. They have different advantages: the Error-Prone check runs at compile-time, and lint will show up in the
IDE and run on Kotlin code. You should use whichever one fits your stack best.

Full integration instructions can be found on their respective wikis:

* https://github.com/uber/AutoDispose/wiki/Lint-Check
* https://github.com/uber/AutoDispose/wiki/Error-Prone

Prior to this release, the Error Prone checker was missing a required service file
to run, so the new artifact is different than the previous one (but not conflicting since the old one never worked!).

This was a major project and contribution from a new maintainer to the project! [@shaishavgandhi05](https://github.com/shaishavgandhi05)

All PRs: [#316](https://github.com/uber/AutoDispose/pull/316), [#315](https://github.com/uber/AutoDispose/pull/315), [#313](https://github.com/uber/AutoDispose/pull/313), [#312](https://github.com/uber/AutoDispose/pull/312), [#310](https://github.com/uber/AutoDispose/pull/310), [#307](https://github.com/uber/AutoDispose/pull/307), [#308](https://github.com/uber/AutoDispose/pull/308), [#306](https://github.com/uber/AutoDispose/pull/306), [#299](https://github.com/uber/AutoDispose/pull/299), [#303](https://github.com/uber/AutoDispose/pull/303), [#301](https://github.com/uber/AutoDispose/pull/301), [#300](https://github.com/uber/AutoDispose/pull/300), [#282](https://github.com/uber/AutoDispose/pull/282), [#291](https://github.com/uber/AutoDispose/pull/291), [#292](https://github.com/uber/AutoDispose/pull/292)

### DefaultLifecycleScopeProvider and KotlinLifecycleScopeProvider Deprecation ([#275](https://github.com/uber/AutoDispose/pull/275))

`DefaultLifecycleScopeProvider` and `KotlinLifecycleScopeProvider` are now deprecated, and their default `requestScope()` behavior now elevated into the based
`LifecycleScopeProvider` class. This is implemented as a Java 8 `default` interface method.

### Misc

* Non-android `-ktx` artifacts now use `implementation`/`api` dependencies ([#277](https://github.com/uber/AutoDispose/pull/277))
* `automatic-module-name` is added to relevant JDK modules ([#281](https://github.com/uber/AutoDispose/pull/281))
* Updated doc on `RxLifecycleInterop` ([#280](https://github.com/uber/AutoDispose/pull/280))
* Kotlin is updated to 1.3.11 [#274](https://github.com/uber/AutoDispose/pull/274), [#309](https://github.com/uber/AutoDispose/pull/309)

Thanks to the following external contributors for this release: [@MarkyC](https://github.com/MarkyC)

Version 1.0.0
----------------------------

_2018-10-10_

* Stable release!
* This is identical in functionality to 1.0.0-RC3 but completely migrated to the new AndroidX artifacts. From this point forward for Android projects, you must be on AndroidX artifacts. You can use 1.0.0-RC3 to ease migration if need be.

Version 1.0.0-RC3
----------------------------

_2018-10-10_

* The project now targets Java 8 bytecode, with the expectation that projects are either on Java 8 or (if on Android) use D8 via Android Gradle Plugin 3.2.0. ([#257](https://github.com/uber/AutoDispose/pull/257))
* More sample recipes for Android ViewModels and Fragments ([#254](https://github.com/uber/AutoDispose/pull/254)) ([#260](https://github.com/uber/AutoDispose/pull/260))
* Various dependency updates

```
Support library 28.0.0
Architecture Components (runtime) 1.1.1
Kotlin 1.2.71
RxJava 2.2.2
RxAndroid 2.1.0
```

Thanks to the following contributors for this release: [@shaishavgandhi05](https://github.com/shaishavgandhi05)

Version 1.0.0-RC2
----------------------------

_2018-8-14_

Small followup update to RC1

* `subscribe(Observer)` methods in `SubscribeProxy` interfaces now accept wildcards for the observer type, matching their RxJava counterparts ([#244](https://github.com/uber/AutoDispose/issues/244))
  * Example: `subscribe(Observer<T> observer)` -> `subscribe(Observer<? super T> observer)`
* Kotlin artifacts now include `Module.md` files in dokka documentation ([#238](https://github.com/uber/AutoDispose/issues/238))
* Android lifecycle Kotlin `scope()` extensions now return `ScopeProvider` instead of `LifecycleScopeProvider` ([#239](https://github.com/uber/AutoDispose/issues/239))

Thanks to the following contributors for this release: [@shaishavgandhi05](https://github.com/shaishavgandhi05)

Version 1.0.0-RC1
----------------------------

_2018-8-2_

This is the first release candidate of AutoDispose 1.0!

### `Completable` replaces `Maybe` as the source of truth for scoping ([#234](https://github.com/uber/AutoDispose/issues/234))

_Note: we say `Completable` for semantic convenience, but in code it's almost always referred to via `CompletableSource` for flexibility_

This is a significant API change, but a good one we want to clean up before releasing 1.0. Since its inception, AutoDispose has always coerced 
scopes into a `Maybe` representation. Now, scopes are coerced to a `CompletableSource`.

`Maybe` seemed like the right idea for something that "may or may not emit", but in our case we actually don't 
care about the difference between onSuccess or onComplete. We did have a notion of "UNBOUND", but that doesn't offer anything other 
than a severed lifecycle scope disposal in an atomic reference (no other cleanups would happen for gc, etc). This brings us to a `Single`. 
The thing is though, we don't care about the object/element type. A `Single` where the type doesn't matter is semantically a `Completable`, 
and thus this change.

Note that semantics are slightly different for anyone that sourced scope via emissions from an `Observable`, `Maybe`, `Completable`, 
or `Flowable`, where before a completion event would not trigger disposal. Now it would. In the lifecycle artifact, completion 
of the lifecycle or emission of the target event (via `takeUntil()`) will signal disposal.

If there's a strong desire for it, we could look at adding top-level `autoDisposable` overrides that accept other RxJava types (and coerce them to `Completable`).

### Lifecycle components are now a separate artifact ([#228](https://github.com/uber/AutoDispose/issues/228))

`LifecycleScopeProvider` is now in a separate artifact under `autodispose-lifecycle`, and now just extends `ScopeProvider`. This is sort of something
we always wanted to do, as the recommended solution for AutoDispose is namely to use `ScopeProvider` and standard RxJava types. `LifecycleScopeProvider`
supports corresponding-events-type lifecycles for use with lifecycle components like Android, but we mostly see this as a mechanism for boundary checks.
Dan Lew excellently discusses this subject in his "[Why Not RxLifecycle?](https://blog.danlew.net/2017/08/02/why-not-rxlifecycle/)" blog post.

This does come with the caveat that one must implement `requestScope()` in implementations now. To smoothen this usage, a `autodispose-lifecycle-jdk8`
artifact exists with a `DefaultLifecycleScopeProvider` that has a `default` implementation of this on Java 8+ that matches the existing behavior. A 
similar default behavior was added for the `autodispose-lifecycle-ktx` artifact. These behaviors can be further tuned via factory helpers in `LifecycleScopes`.

Other notable changes in this:
* `OutsideLifecycleException` has been renamed to `OutsideScopeException` and kept in the core artifact. Boundary checks can be done and respected in `ScopeProvider`
implementations, and corresponding `AutoDisposePlugins` for this have been renamed accordingly.
* `correspondingEvents()` now returns a `CorrespondingEventsFunction`, which is a narrower subtype of `Function` that only needs one generic and only allows for
throwing `OutsideScopeException.

### Misc

* All deprecated APIs have been removed.
* Kotlin Artifacts have been renamed to be `{name}-ktx` instead of `{name}-kotlin` to match other library conventions.
* Kotlin artifacts with `.ktx` or `.kotlin` package name entries have had them removed to match convention with other ktx-style artifacts.
  * i.e. Instead of `com.uber.autodispose.kotlin`, it would just be `com.uber.autodispose`.
* `ViewScopeProvider` now uses a custom `MainThreadDisposable` that respects any main thread checks set via `AutoDisposeAndroidPlugins`. ([#232](https://github.com/uber/AutoDispose/pull/232))
* Jetbrains annotations have been removed in favor of just using RxJava's `@Nullable` annotation directly. Saves some proguard rules and dependencies, and also makes annotation usage consistent. 
* The following dependencies have been updated:
  * RxJava 2.2.0 (`as()` and `ParallelFlowable` are now stable APIs)
  * Kotlin 1.2.60
  * Build against Android SDK 28
  * Support library 27.1.1
  * RxLifecycle 2.2.2
  * RxAndroid 2.0.2
* The sample app has had some wonderful community contributions
  * [LeakCanary integration](https://github.com/uber/AutoDispose/pull/225)
  * [Architecture components sample](https://github.com/uber/AutoDispose/pull/223), including `ViewModel` and using a repository pattern
  * [General structure cleanup](https://github.com/uber/AutoDispose/pull/226)

This is an RC1. We won't release 1.0 final until the AndroidX artifacts are stable to save ourselves from having to release a 2.0 immediately after this. 
These are a lot of breaking changes, so please let us know if you see any issues.

Thanks to the following contributors for this release: [@shaishavgandhi05](https://github.com/shaishavgandhi05) and [@remcomokveld](https://github.com/remcomokveld)

Version 0.8.0
----------------------------

_2018-5-7_

### Deprecated Scoper APIs now use the converter API under the hood ([#188](https://github.com/uber/AutoDispose/issues/188))

Up to this point, the new `as()`-based converter APIs just delegated to the existing deprecated 
`to()` APIs. In this release, they have been flipped, such that the `to()` APIs now just point to 
the `as()`-based APIs. This should be no visible user change, but please let us know if you see any 
issues.

### ViewScopeProvider now implements ScopeProvider instead of LifecycleScopeProvider ([#196](https://github.com/uber/AutoDispose/issues/196))

We believe this makes more sense, as there's no beginning boundary check for Views that we can 
check and the general attach state is quite simple. This also avoids leaking an unnecessary 
internal API.

### Defer to `Comparable` checks if `LifecycleScopeProvider` types implement it ([#196](https://github.com/uber/AutoDispose/issues/196))

For better flexibility, if a type for `LifecycleScopeProvider` implements `Comparable`, we will 
defer to it rather than `equals()`. This allows for consumers to better convey event *ordering* to 
the scope provider, and allow AutoDispose to catch events *after* a target event as a fallback. 
This covers cases where the targeted "end" event is missed but a later event comes through, 
allowing AutoDispose to dispose anyway. Note that this may result in a behavior change if your 
lifecycle types implemented `Comparable` before.

### Removed Error-Prone annotations ([#208](https://github.com/uber/AutoDispose/issues/208))

As of Error-Prone 2.3.1, `@DoNotMock` was removed. We've switched to an internal copy of this 
annotation for documentation purposes and for any external checkers to still check this usage on 
their own (by name).

### Switch from JSR305 to Jetbrains annotations for nullability ([#208](https://github.com/uber/AutoDispose/issues/208))

To be compatible with the Java 9 module system, we've switched away from the JSR 305 
annotations/javax-extras on packages and now use the Jetbrains annotations for nullability instead. 
We still abide by a nonnull-by-default implementation, and only annotate nullable elements with 
`@Nullable`. This dependency, like JSR305/javax-extras, is `compileOnly`.

### Misc changes

* A few miscellaneous IDE warnings ([#208](https://github.com/uber/AutoDispose/issues/208))
* We are now building against Android Gradle Plugin 3.1.x (latest stable) ([#190](https://github.com/uber/AutoDispose/issues/190))
  * Due to ongoing Dokka issues and update latency, we've had to disable it on Kotlin artifacts for 
  now. We plan to re-enable on the next release, which should add compatibility for AGP 3.x+.

### Call for input on next steps

We have two major design proposals that we want community feedback on that would take shape in the 
next couple of releases. Please let us know if you have any thoughts!

* **Kotlin rewrite:** [#198](https://github.com/uber/AutoDispose/issues/198)
* **Extract LifecycleScopeProvider to separate artifact, make it extend ScopeProvider:** [#197](https://github.com/uber/AutoDispose/issues/197)

Thanks to the following contributors for this release: [@tbsandee](https://github.com/tbsandee), 
[@atexannamedbob](https://github.com/atexannamedbob)

Version 0.7.0
----------------------------

_2018-3-26_

### AutoDisposeAndroidPlugins ([#183](https://github.com/uber/AutoDispose/pull/183))

New API! `AutoDisposeAndroidPlugins` API for plugin hooks to AutoDispose's android behavior at runtime. 
The first plugin supported here is `MainThreadChecker`.

This plugin allows for supplying a custom `BooleanSupplier` that can customize how main thread checks 
work. The conventional use case of this is Android JUnit tests, where the `Looper` class is not 
stubbed in the mock android.jar and fails explosively when touched.

Another potential use of this at runtime to customize checks for more fine-grained main thread 
checks behavior.

Example

```java
AutoDisposeAndroidPlugins.setOnCheckMainThread(() -> {
    return true; // Use whatever heuristics you prefer.
})
```

This is available in the `autodispose-android` artifact, and all mainthread-checking APIs in android 
artifacts will delegate to this plugin hook.

### Misc

* Fixed a few nullability and other minor warnings ([#187](https://github.com/uber/autodispose/pull/187))
  * Contributed by [@tbsandee](https://github.com/tbsandee)!

Version 0.6.1
----------------------------

_2018-2-23_

This is patch release with a couple of QoL improvements:
* Android artifacts' consumer proguard rules have been updated to not warn on the compiled error-prone annotations, like `@DoNotMock` ([#178](https://github.com/uber/autodispose/issues/178))
  * Contributed by [@danh32](https://github.com/danh32)!
* Android artifacts no longer bundle a useless `BuildConfig.java` file ([#177](https://github.com/uber/autodispose/issues/177))

Version 0.6.0
----------------------------

_2018-2-5_

### Error-Prone Checker artifact ([#156](https://github.com/uber/autodispose/issues/156))

There is a new Error-Prone checker artifact that you can optionally apply to have error-prone enforced checks that rx chains are autodisposed when executing in a class that has scope. This is experimental in the public, but has been used extensively internally at Uber for nearly a year. Please let us know if you run into any issues!

Wiki page with setup and configuration instructions: https://github.com/uber/AutoDispose/wiki/Error-Prone-Checker

We plan to add a UAST lint artifact in the future as well.

### ParallelFlowable support ([#155](https://github.com/uber/autodispose/issues/155))

AutoDispose now supports RxJava's `ParallelFlowable` type. Note that this only works through the new `as()` API, and there is no `ParallelScoper` API (since those are being removed in 1.0).

### ScopeProvider and LifecycleScopeProvider are now annotated with `@DoNotMock` ([#153](https://github.com/uber/autodispose/issues/153))

These types have specific test helpers that will be more robust for long term test usage, and thus should not be mocked.

### Convenience `test()` methods added to all SubscribeProxy interfaces ([#160](https://github.com/uber/autodispose/issues/160))

These are to match the convenience `test()` methods in regular RxJava types.

### Misc

- Archcomponents updated to 1.1.0 for compatibility with new artifacts ([#128](https://github.com/uber/autodispose/issues/128))
- `autodispose-android-archcomponents-test` and `autodispose-android-archcomponents-test-kotlin` now only depend on the `common` arch components artifact rather than `extensions`, which removes the unused `livedata` and `viewmodel` transitive dependencies.
- RxViewHolder examples now implement `LifecycleScopeProvider` instead of `ScopeProvider` ([#157](https://github.com/uber/autodispose/issues/157))
- Deprecated Kotlin APIs are now `ERROR` level instead of `WARNING` ([#151](https://github.com/uber/autodispose/issues/151))
- Various doc fixes ([#158](https://github.com/uber/autodispose/issues/158))
- RxLifecycle updated to 2.2.1 ([#161](https://github.com/uber/autodispose/issues/161))
- ErrorProne annotations updated to 2.2.0 ([#161](https://github.com/uber/autodispose/issues/161))
- Android artifacts now compiled against SDK 27
- Android support annotations updated to 27.0.2

Thanks to the following contributors! [@VisheshVadhera](https://github.com/VisheshVadhera) [@bangarharshit](https://github.com/bangarharshit) [@mmallozzi](https://github.com/mmallozzi) [@0legg](https://github.com/0legg) [@shaunkawano](https://github.com/shaunkawano) 

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
