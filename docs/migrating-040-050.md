0.5.0 switched the API to the new `autoDisposable()` static factories in `AutoDispose.java`. To migrate, you can leverage IntelliJ's structural replace. Below is an exhaustive list. I suspect this could be whittled down to `Completable` replacement and java 7/8 generic templates, but my script text fu isn't good enough (contributions welcome!).

Notes for structural replace. If using Java 8, replace `<$Type$>` with `<>` and remove `<$Type>` from the replacement template. Would be ideal to combine these into one smart replace but the tooling is a little rough to debug.

## Flowable
### Java 8 / `Object` streams
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forFlowable())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.autoDisposable($Scope$))
```

### Java 7 regular types
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).<$Type$>forFlowable())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.<$Type$>autoDisposable($Scope$))
```

## Observable
### Java 8 / `Object` streams
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forObservable())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.autoDisposable($Scope$))
```

### Java 7 regular types
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).<$Type$>forObservable())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.<$Type$>autoDisposable($Scope$))
```

## Maybe
### Java 8 / `Object` streams
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forMaybe())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.autoDisposable($Scope$))
```

### Java 7 regular types
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).<$Type$>forMaybe())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.<$Type$>autoDisposable($Scope$))
```

## Single
### Java 8 / `Object` streams
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forSingle())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.autoDisposable($Scope$))
```

### Java 7 regular types
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).<$Type$>forSingle())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.<$Type$>autoDisposable($Scope$))
```

## Completable
Search template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forCompletable())
```
Replacement template
```
$Stream$.as(com.uber.autodispose.AutoDispose.autoDisposable($Scope$))
```
