AutoDispose
===========

**AutoDispose** is a tool for automatically binding the execution of RxJava 2 streams to a provided
scope via disposal/cancellation.

Overview
--------

The idea is simple: construct your chain like any other, and then at subscription you simply prefix your
observer/consumer/etc implementations with a wrapping `AutoDispose` call. In every day use, it 
 usually look like this: 

```java
myObservable
    .doStuff()
    .subscribe(AutoDispose
        .observable()           // The RxJava type
        .scopeTo(this)          // The scope, can be a Maybe<?> or a ScopeProvider<?>
        .around(s -> ...));     // Your usual implementation
```

#### Scope

`scopeTo` accepts two overloads: `Maybe` and `ScopeProvider`. The `Maybe` semantic is modeled after
the `takeUntil()` operator, which accepts an `Observable` whose first emission is used as a notification
to signal completion. This is is logically the behavior of a `Maybe`, so we choose to make that explicit.

The alternative option of a `ScopeProvider` allows you to pass in an interface of something can provide
a resolvable scope. A common use case for this is objects that have implicit lifecycles, such as Android's
`Activity`, `Fragment`, and `View` classes. Internally at subscription-time, `AutoDispose` will resolve
a `Maybe` representation of the target `end` event in the lifecycle, and exposes an API to dictate what
corresponding events are for the current lifecycle state (e.g. `ATTACH` -> `DETACH`). This also allows
you to enforce lifecycle boundary requirements, and by default will error if the lifecycle has either
not started yet or has already ended.

#### "around"

Every type has some number of `around` overloads. These simply match the available `subscribe` signatures
for the observed type.

#### Behavior

The created observer encapsulates the parameters of `around` to create a disposable, auto-disposing
observer that acts as a lambda observer (passthrough) unless the underlying lifecycle `Maybe` emits.
Both lifecycle end and upstream termination result in immediate disposable of both the underlying lifecycle
subscription and upstream disposable.

#### Support

`Flowable`, `Observable`, `Maybe`, `Single`, and `Completable` are all supported. Implementation is solely
based on their `Observer` types, so conceivably any type that uses those for subscription should work.

There is a separate Android artifact with extra APIs for Android components, such as support for `View`
lifecycle binding.

### Motivations

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
 upon lifecycle end. We're quite happy with it, and hope the community finds it useful as well.
 
Special thanks go to [Dan Lew][dan] (creator of RxLifecycle), whom I've had many discussions with 
around lifecycle handling in RxJava with over the past couple years and learned a lot from. Much of 
the internal lifecycle resolution mechanics of `AutoDispose` are inspired by RxLifecycle.
 
### RxJava 1

This pattern is *sort of* possible in RxJava 1, but only on `Subscriber` (via `onStart()`) and 
`CompletableObserver` (which matches RxJava 2's API). We are aggressively migrating our internal code
 to RxJava 2, and do not plan to try to backport this to RxJava 1.

Download
--------

Java:
```gradle
compile 'com.uber.autodispose:autodispose:TODO'
```

Android components:
```gradle
compile 'com.uber.autodispose:autodispose-android:TODO'
```

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
 [snapshots]: https://oss.sonatype.org/content/repositories/snapshots/
