Changelog
=========

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
