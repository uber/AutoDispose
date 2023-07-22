`AutoDispose` is an [Error-Prone](https://github.com/google/error-prone)
check to detect missing AutoDispose scope within defined scoped elements.

## Installation

Below are sample configurations which pull in both the AutoDispose Error-Prone checker.

### Gradle

#### Java

```gradle
plugins {
  id "java-library" // Or whatever other java plugin you're using
  id "net.ltgt.errorprone" version "0.6"
}

dependencies {
  errorprone "com.uber.autodispose:autodispose-error-prone:x.y.z" // where x.y.z is the latest version.
  errorprone "com.google.errorprone:error_prone_core:2.3.2" // Or whatever the latest version is
}

tasks.withType(JavaCompile).configureEach {
  // Only if you want to support custom types with scopes
  // Below is a sample configuration which includes Conductor
  def classesWithScope = [
      "com.bluelinelabs.conductor.Controller"
  ]
  options.errorprone {
    check("AutoDispose", CheckSeverity.ERROR)
    option("AutoDispose:TypesWithScope", classesWithScope.join(","))
    option("UAutoDispose:Lenient", "true")
  }
}
```

#### Android

```gradle
plugins {
  id "net.ltgt.errorprone" version "0.0.13"
}

dependencies {
  errorprone "com.uber.autodispose:autodispose-error-prone-checker:x.y.z" // where x.y.z is the latest version.
  errorprone "com.google.errorprone:error_prone_core:2.3.2" // Or whatever the latest version is
}

// Must go in afterEvaluate
afterEvaluate {
  tasks.withType(JavaCompile).configureEach {
    // Only if you want to support custom types with scopes
    // Below is a sample configuration which includes Conductor
    def classesWithScope = [
        "com.bluelinelabs.conductor.Controller"
    ]
    options.errorprone {
      check("AutoDispose", CheckSeverity.ERROR)
      option("AutoDispose:TypesWithScope", classesWithScope.join(","))
      option("AutoDispose:Lenient", "true")
    }
  }
}
```

### Maven

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.5</version>
      <configuration>
        <compilerId>javac-with-errorprone</compilerId>
        <forceJavacCompilerUse>true</forceJavacCompilerUse>
        <source>1.8</source>
        <target>1.8</target>
        <showWarnings>true</showWarnings>
        <annotationProcessorPaths>
          <path>
             <groupId>com.uber.autodispose</groupId>
             <artifactId>autodispose-error-prone</artifactId>
             <version>x.y.z</version>
          </path>
        </annotationProcessorPaths>
        <compilerArgs>
          <!-- Only if you want to support custom configuration
          Below is a sample configuration which includes Conductor -->
          <arg>--XepOpt:AutoDispose:TypesWithScope=com.bluelinelabs.conductor.Controller</arg>
          <arg>--XepOpt:AutoDispose:Lenient=true</arg>
        </compilerArgs>
      </configuration>
      <dependencies>
        <dependency>
          <groupId>org.codehaus.plexus</groupId>
          <artifactId>plexus-compiler-javac-errorprone</artifactId>
          <version>2.8</version>
        </dependency>
        <!-- override plexus-compiler-javac-errorprone's dependency on
             Error Prone with the latest version -->
        <dependency>
          <groupId>com.google.errorprone</groupId>
          <artifactId>error_prone_core</artifactId>
          <version>2.3.2</version>
        </dependency>
      </dependencies>        
    </plugin>
</build>
```

## Report example

The following code snippet:

```java
public class ComponentWithLifecycle extends Activity {
  public void observeOnSomething() {
    Observable
        .interval(1, TimeUnit.SECONDS)
        .subscribe(new Consumer<Long>() {
          @Override public void accept(Long interval) throws Exception {
            System.out.println(interval);
          }
        });
  }
}
```

would produce the following error:

```
./gradlew build
error: [AutoDispose] Missing Disposable handling: Apply AutoDispose or cache the Disposable instance manually and enable lenient mode.
        .subscribe(new Consumer<Long>() {
                  ^
    (see https://github.com/uber/AutoDispose/wiki/Error-Prone-Checker)
```

Would lead to this error at compile-time.

## Configuration

### Scopes

By default the checker is applied to AutoDispose interfaces and standard Android components with lifecycles:
1. [Activity](https://developer.android.com/reference/android/app/Activity.html)
2. [Fragment](https://developer.android.com/reference/android/app/Fragment.html)
3. [Support Fragment](https://developer.android.com/reference/android/support/v4/app/Fragment.html)
4. [LifecycleScopeProvider](https://uber.github.io/AutoDispose/1.x/autodispose/com/uber/autodispose/LifecycleScopeProvider.html)
5. [ScopeProvider](https://uber.github.io/AutoDispose/1.x/autodispose/com/uber/autodispose/ScopeProvider.html)
6. [LifecycleOwner](https://developer.android.com/reference/android/arch/lifecycle/LifecycleOwner.html)

This can be configured by [Error-Prone's command line flags](https://errorprone.info/docs/flags). The following flag is supported and takes input in a form of comma separated list of fully qualified class names of classes with scopes:

```
-XepOpt:AutoDispose:TypesWithScope=com.bluelinelabs.conductor.Controller,android.app.Activity
```

This flag adds the provided custom scopes to the default scopes mentioned above.

### Overriding Scopes

If you only want the error prone check to run on your custom scopes and not the default ones, you can simply override the default scopes by adding the `OverrideScopes` flag like so:
```
-XepOpt:AutoDispose:OverrideScopes=true
```

### Lenient

`Lenient` is a mode to ask the checker to be lenient when capturing returned `Disposable` types. What this means is that if an rx `subscribe` method is called and its returned `Disposable` is captured, AutoDispose this code is manually managing the subscription and show ignore it. The same applies for capturing the returned value of `subscribeWith` if the input type implements `Disposable`.

This can be configured by [Error-Prone's command line flags](https://errorprone.info/docs/flags). The following flag is supported and takes input in a form of a boolean `true` or `false`:

```
-XepOpt:AutoDispose:Lenient=true
```

The default value of this is `false`.

**Examples**

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
```
