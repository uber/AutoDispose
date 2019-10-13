Migrating from AutoDispose 1.x to 2.x

AutoDispose 2.x is built against RxJava 3.x and is binary-compatible with AutoDispose 1.x and RxJava 2.x. As such - it has a different package name and maven group ID.

## Packaging

|  | Maven Group ID | Package Name |
| --- | --- | --- |
| 1.x | `com.uber.autodispose` | `com.uber.autodispose` |
| 2.x | `com.uber.autodispose2` | `autodispose2` |

For any sub-packages, the above mapping should be used for those package prefix replacements as well.

## Changes

-   All deprecated APIs in 1.x have been removed. This consisted exclusively of deprecated Kotlin `autoDisposable` extension functions that were deprecated in 1.4.0.
-   The `autodispose-android-archcomponents*` artifacts have been renamed to to `autodispose-androidx-lifecycle*` to match the `androidx-lifecycle` library they correspond to.
-   The lint and error prone checks have also been updated. We may look at consolidating these before 2.0 final is released if the community wants.
-   At the time of writing, there is no RxLifecycle with RxJava 3 support yet, and as such there is no AutoDispose 2.x interop artifact for RxLifecycle. We can add this back if there's a new RxLifecycle release with RxJava 3.x support.
