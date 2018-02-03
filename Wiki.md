# Error Prone Checker

UseAutoDispose is an [Error-Prone](https://github.com/google/error-prone)
check to detect missing AutoDispose scope within defined scoped elements.

## Installation

Here are sample configurations which pulls in both the ErrorProne and the AutoDispose check.

### Gradle

```gradle
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
}

plugins {
  // we assume you are already using the Java plugin
  id "net.ltgt.apt" version "0.13"
  id "net.ltgt.errorprone" version "0.0.13"
}

dependencies {
  apt "com.uber.autodispose:autodispose-error-prone-checker:x.y.z" // where x.y.z is the latest version.

  errorprone "com.google.errorprone:error_prone_core:2.1.3"
}

tasks.withType(JavaCompile) {
  // Only if you want to support custom configuration
  // Below is a sample configuration which include Conductor and Activity
  options.compilerArgs += ["-XepOpt:ClassesWithScope"
                                       + "=com.bluelinelabs.conductor.Controller,android.app.Activity"]
}
```

For Android:

```gradle
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
}

plugins {
  // we assume you are already using the Java plugin
  id "net.ltgt.errorprone" version "0.0.13"
}

dependencies {
  annotationProcessor "com.uber.autodispose:autodispose-error-prone-checker:x.y.z" // where x.y.z is the latest version.

  errorprone "com.google.errorprone:error_prone_core:2.1.3"
}

tasks.withType(JavaCompile) {
  // Only if you want to support custom configuration
  // Below is a sample configuration which include Conductor and Activity
  options.compilerArgs += ["-XepOpt:ClassesWithScope"
                                       + "=com.bluelinelabs.conductor.Controller,android.app.Activity"]
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
             <artifactId>autodispose-error-prone-checker</artifactId>
             <version>x.y.z</version>
          </path>
        </annotationProcessorPaths>
        <compilerArgs>
          <!-- Only if you want to support custom configuration
          Below is a sample configuration which include Conductor and Activity -->
          <arg>--XepOpt:ClassesWithScope=com.bluelinelabs.conductor.Controller,android.app.Activity</arg>
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
          <version>2.1.3</version>
        </dependency>
      </dependencies>        
    </plugin>
</build>
```

## Code example

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

```
./gradlew build
error: [UseAutoDispose] Always apply an AutoDispose scope before subscribing within defined scoped elements.
        .subscribe(new Consumer<Long>() {
                  ^
    (see https://github.com/uber/AutoDispose/wiki/Error-Prone-Checker)
```
Would lead to this error at compile-time.

## Configuration

By default the checker is applied to standard android components with lifecycle and AutoDispose interfaces:
1. [Activity](https://developer.android.com/reference/android/app/Activity.html)
2. [Fragment](https://developer.android.com/reference/android/app/Fragment.html)
3. [Support Fragment](https://developer.android.com/reference/android/support/v4/app/Fragment.html)
4. [LifecycleScopeProvider](https://uber.github.io/AutoDispose/0.x/autodispose/com/uber/autodispose/LifecycleScopeProvider.html)
5. [ScopeProvider](https://uber.github.io/AutoDispose/0.x/autodispose/com/uber/autodispose/ScopeProvider.html)
6. [LifecycleOwner](https://developer.android.com/reference/android/arch/lifecycle/LifecycleOwner.html)

It can be configured by [Error-Prone's command line flags](http://errorprone.info/docs/flags).

The following flag is supported and takes input in a form of comma separated list of fully qualified class names of Classes with scopes:

- `-XepOpt:ClassesWithScope=com.bluelinelabs.conductor.Controller,android.app.Activity`

In this case, the check is now applied to `Controller` and `Activity` **only**.
