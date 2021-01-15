`AutoDisposeDetector` is a lint check to detect missing AutoDispose scope within defined scoped elements. 

## Installation

For Android Java/Kotlin projects, no configuration is required as the AutoDispose lint check is run by default with the existing lint checks. 

## Report example

The following code snippet:
```kotlin
class ComponentWithLifecycle : Activity {
  
  fun observeOnSomething() {
    Observable
        .interval(1, TimeUnit.SECONDS)
        .subscribe { println(it) }
  }
}
```
will produce the following error at compile-time:
```console
./gradlew build
src/com/sample/app/ComponentWithLifecycle.kt:5: Error: Missing Disposable handling: Apply AutoDispose or cache the Disposable instance manually and enable lenient mode. [AutoDispose]
        .subscribe { println(it) }
        ~~~~~~~~~~~~~~~~~~~~~~~~~~
1 errors, 0 warnings
```

## Configuration

### Scopes

By default, the lint check is applied to AutoDispose interfaces and standard Android components with lifecycles:
1. [Activity](https://developer.android.com/reference/android/app/Activity.html)
2. [Fragment](https://developer.android.com/reference/android/app/Fragment.html)
3. [Support Fragment](https://developer.android.com/reference/android/support/v4/app/Fragment.html)
4. [ScopeProvider](https://uber.github.io/AutoDispose/1.x/autodispose/com/uber/autodispose/ScopeProvider.html) (which implicitly includes [LifecycleScopeProvider](https://uber.github.io/AutoDispose/1.x/autodispose/com/uber/autodispose/LifecycleScopeProvider.html))
5. [LifecycleOwner](https://developer.android.com/reference/android/arch/lifecycle/LifecycleOwner.html)

You can add your own custom scopes that you want the lint check applied to. In your **app-level** `gradle.properties` file, add the fully qualified name of your custom classes as comma-separated-values like so:
```groovy
autodispose.typesWithScope=com.bluelinelabs.conductor.Controller,com.sample.app.BasePresenter
```
The types supplied are then added to the default types listed above. 

### Overriding Scopes

If you only want the lint check to run on your custom scopes and not the default ones, you can simply override the default scopes by adding this in your **app-level** `gradle.properties` file:
```groovy
autodispose.overrideScopes=true
```

### Lenient

`Lenient` is a mode to ask the checker to be lenient when capturing returned Disposable types. What this means is that if an rx subscribe method is called and its returned Disposable is captured, AutoDispose this code is manually managing the subscription and show ignore it. The same applies for capturing the returned value of subscribeWith if the input type implements Disposable.

This can be configured by adding the following flag to the **app-level** `gradle.properties` file. 
```groovy
autodispose.lenient=true
```

The default value of this is `false`. 

### Kotlin Extension

By default, subscribe and subscribeWith methods are checked. To support other subscribe methods such as subscribeBy in RxKotlin, you can add your own subscribe extensions.
In your **app-level** `gradle.properties` files, add kotlin extension functions in format of `{full package name for extension's scope}#{functionName}` and comma-separated-values like so:
```groovy
autodispose.kotlinExtensionFunctions="io.reactivex.rxjava3.kotlin.subscribers#subscribeBy,com.sample.app.SubscribeExt#subscribe2"
```

#### Examples
```java
// This is allowed in lenient mode
Disposable d = Observable.just(1).subscribe();

// This is allowed in lenient mode, because the subscribeWith arg type is Disposable
DisposableObserver<Integer> do = Observable.just(1).subscribeWith(new DisposableObserver...)

// This is not allowed in lenient mode, because the subscribeWith arg type is not Disposable
Observer<Integer> do = Observable.just(1).subscribeWith(new Observer...)

// This is not allowed in lenient mode, because the return value is not captured
Observable.just(1).subscribe();

// This is not allowed in lenient mode, because that subscribe() overload just returns void
Observable.just(1).subscribe(new Observer...)

// This is not allowed when kotlin extension functions option is used
Observable.just(1).subscribeBy { }
```
