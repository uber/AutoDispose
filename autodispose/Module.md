# Module autodispose

AutoDispose is an RxJava 2 tool for automatically binding the execution of RxJava 2 streams to a
provided scope via disposal/cancellation.

The idea is simple: construct your chain like any other, and then at subscription you simply
drop in the relevant factory call + method for that type as a converter. In everyday use, it
usually looks like this:

```
myObservable
	.doStuff()
	.as(autoDisposable(this))   // <-- AutoDispose
	.subscribe(s -> ...);
```

By doing this, you will automatically unsubscribe from `myObservable` as indicated by your scope
- this helps prevent many classes of errors when an observable emits and item, but the actions
taken in the subscription are no longer valid. For instance, if a network request comes back
after a UI has already been torn down, the UI can't be updated - this pattern prevents this type
of bug.

# Package autodispose2

Core implementation.

# Package autodispose2.observers

These are observers AutoDispose uses when scoping an observable. They are exposed as a public API
to allow for consumers to watch for them if they want, such as in RxJava plugins.
