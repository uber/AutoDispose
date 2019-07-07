0.3.0 switched the API to the new static factories in `AutoDispose.java`. To migrate, you can leverage IntelliJ's structural replace. Below is an exhaustive list. I suspect this could be whittled down to `Completable` replacement and java 7/8 generic templates, but my script text fu isn't good enough (contributions welcome!).

Notes for structural replace. If using Java 8, replace `<$Type$>` with `<>` and remove `<$Type>` from the replacement template. Would be ideal to combine these into one smart replace but the tooling is a little rough to debug.

## Flowable
### Java 8 / `Object` streams
Search template
```
$Stream$.to(new com.uber.autodispose.FlowableScoper<>($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forFlowable())
```

### Java 7 regular types
Search template
```
$Stream$.to(new com.uber.autodispose.FlowableScoper<$Type$>($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).<$Type$>forFlowable())
```

## Observable
### Java 8 / `Object` streams
Search template
```
$Stream$.to(new com.uber.autodispose.ObservableScoper<>($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forObservable())
```

### Java 7 regular types
Search template
```
$Stream$.to(new com.uber.autodispose.ObservableScoper<$Type$>($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).<$Type$>forObservable())
```

## Maybe
### Java 8 / `Object` streams
Search template
```
$Stream$.to(new com.uber.autodispose.MaybeScoper<>($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forMaybe())
```

### Java 7 regular types
Search template
```
$Stream$.to(new com.uber.autodispose.MaybeScoper<$Type$>($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).<$Type$>forMaybe())
```

## Single
### Java 8 / `Object` streams
Search template
```
$Stream$.to(new com.uber.autodispose.SingleScoper<>($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forSingle())
```

### Java 7 regular types
Search template
```
$Stream$.to(new com.uber.autodispose.SingleScoper<$Type$>($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).<$Type$>forSingle())
```

## Completable
Search template
```
$Stream$.to(new com.uber.autodispose.CompletableScoper($Scope$))
```
Replacement template
```
$Stream$.to(com.uber.autodispose.AutoDispose.with($Scope$).forCompletable())
```
