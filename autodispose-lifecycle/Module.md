# Module autodispose-lifecycle

```
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

To simplify implementations, there's an included `LifecycleScopes` utility class with factories
for generating `CompletableSource` representations from `LifecycleScopeProvider` instances.
