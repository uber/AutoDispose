/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.LibraryReferenceTestFile
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
internal class AutoDisposeDetectorTest : LintDetectorTest() {

  companion object {
    // Stub activity
    private val ACTIVITY = java(
      """
      package androidx.appcompat.app;
      import androidx.lifecycle.LifecycleOwner;

      public class AppCompatActivity implements LifecycleOwner {
      }
    """
    ).indented()

    // Stub LifecycleOwner
    private val LIFECYCLE_OWNER = java(
      """
      package androidx.lifecycle;

      public interface LifecycleOwner {}
    """
    ).indented()

    // Stub Fragment
    private val FRAGMENT = java(
      """
      package androidx.fragment.app;
      import androidx.lifecycle.LifecycleOwner;

      public class Fragment implements LifecycleOwner {}
    """
    ).indented()

    // Stub Scope Provider
    private val SCOPE_PROVIDER = kotlin(
      """
      package autodispose2

      interface ScopeProvider
    """
    ).indented()

    // Stub LifecycleScopeProvider
    private val LIFECYCLE_SCOPE_PROVIDER = kotlin(
      """
      package autodispose2.lifecycle
      import autodispose2.ScopeProvider

      interface LifecycleScopeProvider: ScopeProvider
    """
    ).indented()

    // Custom Scope
    private val CUSTOM_SCOPE = kotlin(
      """
      package autodispose2.sample

      class ClassWithCustomScope
    """
    ).indented()

    private val AUTODISPOSE_CONTEXT = kotlin(
      """
      package autodispose2

      interface AutoDisposeContext
    """
    ).indented()

    private val WITH_SCOPE_PROVIDER = kotlin(
      "autodispose2/KotlinExtensions.kt",
      """
          @file:JvmName("KotlinExtensions")
          package autodispose2

          fun withScope(scope: ScopeProvider, body: AutoDisposeContext.() -> Unit) {
          }
        """
    ).indented().within("src/")

    private val WITH_SCOPE_COMPLETABLE = kotlin(
      "autodispose2/KotlinExtensions.kt",
      """
          @file:JvmName("KotlinExtensions")
          package autodispose2

          import io.reactivex.rxjava3.core.Completable

          fun withScope(scope: Completable, body: AutoDisposeContext.() -> Unit) {
          }
        """
    ).indented().within("src/")

    private val RX_KOTLIN = kotlin(
      "io/reactivex/rxjava3/kotlin/subscribers.kt",
      """
          @file:JvmName("subscribers")
          package io.reactivex.rxjava3.kotlin

          import io.reactivex.rxjava3.core.*
          import io.reactivex.rxjava3.disposables.Disposable

          fun <G : Any> Observable<G>.subscribeBy(
                  onNext: (G) -> Unit
          ): Disposable = subscribe()
        """
    ).indented().within("src/")

    private fun propertiesFile(lenient: Boolean = true, kotlinExtensionFunctions: String? = null): TestFile.PropertyTestFile {
      val properties = projectProperties()
      properties.property(LENIENT, lenient.toString())
      kotlinExtensionFunctions?.also {
        properties.property(KOTLIN_EXTENSION_FUNCTIONS, it)
      }
      properties.to(AutoDisposeDetector.PROPERTY_FILE)
      return properties
    }

    private fun lenientPropertiesFile(lenient: Boolean = true): TestFile.PropertyTestFile {
      return propertiesFile(lenient)
    }
  }

  @Test fun observableErrorsOutOnOmittingAutoDispose() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_OWNER,
        FRAGMENT,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Observable;
          import androidx.fragment.app.Fragment;

          class ExampleClass extends Fragment {
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              obs.subscribe();
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.java:8: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    obs.subscribe();
          |    ~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun observableDisposesSubscriptionJava() {
    lint()
      .files(
        *jars(),
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Observable;
          import autodispose2.AutoDispose;
          import autodispose2.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              obs.as(AutoDispose.autoDisposable(scopeProvider)).subscribe();
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Lint 30 doesn't understand subscribeProxies for some reason
      .run()
      .expectClean()
  }

  @Test fun observableSubscribeWithNotHandled() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_OWNER,
        FRAGMENT,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Observable;
          import io.reactivex.rxjava3.observers.DisposableObserver;
          import androidx.fragment.app.Fragment;

          class ExampleClass extends Fragment {
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              obs.subscribeWith(new DisposableObserver<Integer>() {
                @Override
                public void onNext(Integer integer) {
                }

                @Override
                public void onError(Throwable e) {}

                @Override
                public void onComplete() {}
              });
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.java:9: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    obs.subscribeWith(new DisposableObserver<Integer>() {
          |    ^
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun observableSubscribeWithDisposed() {
    lint()
      .files(
        *jars(),
        SCOPE_PROVIDER,
        LIFECYCLE_OWNER,
        FRAGMENT,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Observable;
          import io.reactivex.rxjava3.observers.DisposableObserver;
          import androidx.fragment.app.Fragment;
          import autodispose2.AutoDispose;
          import autodispose2.ScopeProvider;

          class ExampleClass extends Fragment {
            ScopeProvider scopeProvider;
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              obs.as(AutoDispose.autoDisposable(scopeProvider)).subscribeWith(
              new DisposableObserver<Integer>() {
                @Override
                public void onNext(Integer integer) {
                }

                @Override
                public void onError(Throwable e) {}

                @Override
                public void onComplete() {}
              });
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Lint 30 doesn't understand subscribeWith for some reason
      .run()
      .expectClean()
  }

  @Test fun observableDisposesSubscriptionKotlin() {
    lint()
      .files(
        *jars(),
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Observable
          import autodispose2.autoDispose
          import autodispose2.ScopeProvider

          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val obs = Observable.just(1, 2, 3, 4)
              obs.autoDispose(scopeProvider).subscribe()
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  @Test fun singleErrorsOutOnOmittingAutoDispose() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_OWNER,
        ACTIVITY,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Single;
          import androidx.appcompat.app.AppCompatActivity;

          class ExampleClass extends AppCompatActivity {
            void names() {
              Single<Integer> single = Single.just(1);
              single.subscribe();
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.java:8: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    single.subscribe();
          |    ~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun singleDisposesSubscriptionJava() {
    lint()
      .files(
        *jars(),
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Single;
          import autodispose2.AutoDispose;
          import autodispose2.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Single<Integer> single = Single.just(1);
              single.as(AutoDispose.autoDisposable(scopeProvider)).subscribe();
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Lint 30 doesn't understand subscribeProxies for some reason
      .run()
      .expectClean()
  }

  @Test fun singleDisposesSubscriptionKotlin() {
    lint()
      .files(
        *jars(),
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Single
          import autodispose2.ScopeProvider
          import autodispose2.autoDispose

          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val single = Single.just(1)
              single.autoDispose(scopeProvider).subscribe()
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  @Test fun flowableErrorsOutOnOmittingAutoDispose() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_OWNER,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Flowable;
          import androidx.lifecycle.LifecycleOwner;

          class ExampleClass implements LifecycleOwner {
            void names() {
              Flowable<Integer> flowable = Flowable.just(1);
              flowable.subscribe();
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.java:8: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    flowable.subscribe();
          |    ~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun flowableDisposesSubscriptionJava() {
    lint()
      .files(
        *jars(),
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Flowable;
          import autodispose2.AutoDispose;
          import autodispose2.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4);
              flowable.as(AutoDispose.autoDisposable(scopeProvider)).subscribe();
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Lint 30 doesn't understand subscribeProxies for some reason
      .run()
      .expectClean()
  }

  @Test fun flowableDisposesSubscriptionKotlin() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_SCOPE_PROVIDER,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Observable
          import autodispose2.autoDispose
          import autodispose2.lifecycle.LifecycleScopeProvider

          class ExampleClass: LifecycleScopeProvider {
            fun names() {
              val flowable = Flowable.just(1, 2, 3, 4)
              flowable.autoDispose(this).subscribe()
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  @Test fun completableErrorsOutOnOmittingAutoDispose() {
    lint()
      .files(
        *jars(),
        SCOPE_PROVIDER,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Completable
          import autodispose2.ScopeProvider

          class ExampleClass: ScopeProvider {
            fun names() {
              val completable = Completable.complete()
              completable.subscribe()
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.kt:8: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    completable.subscribe()
          |    ~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun completableSubscriptionNonScopedClass() {
    lint()
      .files(
        *jars(),
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Completable;
          import autodispose2.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Completable completable = Completable.complete();
              completable.subscribe();
            }
          }
        """
        ).indented()
      )
      .run()
      .expectClean()
  }

  @Test fun completableDisposesSubscriptionKotlin() {
    lint()
      .files(
        *jars(),
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Observable
          import autodispose2.ScopeProvider
          import autodispose2.autoDispose

          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val completable = Completable.complete()
              completable.autoDispose(scopeProvider).subscribe()
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  @Test fun maybeErrorsOutOnOmittingAutoDispose() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_OWNER,
        ACTIVITY,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Maybe
          import androidx.appcompat.app.AppCompatActivity

          class ExampleClass: AppCompatActivity {
            fun names() {
              val maybe = Maybe.just(1)
              maybe.subscribe()
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.kt:8: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    maybe.subscribe()
          |    ~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun maybeDisposesSubscriptionJava() {
    lint()
      .files(
        *jars(),
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Maybe;
          import autodispose2.AutoDispose;
          import autodispose2.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Maybe<Integer> maybe = Maybe.just(1);
              maybe.as(AutoDispose.autoDisposable(scopeProvider)).subscribe();
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Lint 30 doesn't understand subscribeProxies for some reason
      .run()
      .expectClean()
  }

  @Test fun maybeDisposesSubscriptionKotlin() {
    lint()
      .files(
        *jars(),
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Maybe
          import autodispose2.autoDispose
          import autodispose2.ScopeProvider

          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val maybe = Maybe.just(2)
              maybe.autoDispose(scopeProvider).subscribe()
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  @Test fun customScopeWithoutAutoDispose() {
    val properties = projectProperties()
    properties.property(CUSTOM_SCOPE_KEY, "autodispose2.sample.ClassWithCustomScope")
    properties.to(AutoDisposeDetector.PROPERTY_FILE)

    lint().files(
      *jars(),
      CUSTOM_SCOPE,
      properties,
      kotlin(
        """
      package autodispose2.sample
      import autodispose2.sample.ClassWithCustomScope
      import io.reactivex.rxjava3.core.Observable

      class MyCustomClass: ClassWithCustomScope {
        fun doSomething() {
          val observable = Observable.just(1, 2, 3)
          observable.subscribe()
        }
      }
    """
      ).indented()
    )
      .run()
      .expect(
        """
          src/autodispose2/sample/MyCustomClass.kt:8: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    observable.subscribe()
          |    ~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun customScopeWithAutoDispose() {
    val properties = projectProperties()
    properties.property(CUSTOM_SCOPE_KEY, "autodispose2.sample.ClassWithCustomScope")
    properties.to(AutoDisposeDetector.PROPERTY_FILE)

    lint().files(
      *jars(),
      CUSTOM_SCOPE,
      properties,
      kotlin(
        """
      package autodispose2.sample
      import autodispose2.sample.ClassWithCustomScope
      import io.reactivex.rxjava3.core.Observable
      import autodispose2.autoDispose
      import autodispose2.ScopeProvider

      class MyCustomClass: ClassWithCustomScope {
        lateinit var scopeProvider: ScopeProvider
        fun doSomething() {
          val observable = Observable.just(1, 2, 3)
          observable.autoDispose(scopeProvider).subscribe()
        }
      }
    """
      ).indented()
    )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  @Test fun emptyCustomScopeWithoutAutoDispose() {
    val properties = projectProperties()
    properties.to(AutoDisposeDetector.PROPERTY_FILE)

    lint().files(
      *jars(),
      CUSTOM_SCOPE,
      properties,
      kotlin(
        """
      package autodispose2.sample
      import autodispose2.sample.ClassWithCustomScope
      import io.reactivex.rxjava3.core.Observable
      import autodispose2.ScopeProvider

      class MyCustomClass: ClassWithCustomScope {
        lateinit var scopeProvider: ScopeProvider
        fun doSomething() {
          val observable = Observable.just(1, 2, 3)
          observable.subscribe() // No error since custom scope not defined in properties file.
        }
      }
    """
      ).indented()
    )
      .run()
      .expectClean()
  }

  @Test fun overrideCustomScopeWithoutAutoDispose() {
    val properties = projectProperties()
    properties.property(CUSTOM_SCOPE_KEY, "autodispose2.sample.ClassWithCustomScope")
    properties.property(OVERRIDE_SCOPES, "true")
    properties.to(AutoDisposeDetector.PROPERTY_FILE)

    lint().files(
      *jars(),
      LIFECYCLE_OWNER,
      ACTIVITY,
      properties,
      kotlin(
        """
      package autodispose2.sample
      import autodispose2.sample.ClassWithCustomScope
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import autodispose2.ScopeProvider

      class MyCustomClass: AppCompatActivity {
        lateinit var scopeProvider: ScopeProvider
        fun doSomething() {
          val observable = Observable.just(1, 2, 3)
          observable.subscribe() // No error since the scopes are being overriden and only custom ones are considered.
        }
      }
    """
      ).indented()
    )
      .allowCompilationErrors() // TODO or replace with a real sample stub?
      .run()
      .expectClean()
  }

  @Test fun overrideCustomScopeWithAutoDispose() {
    val properties = projectProperties()
    properties.property(CUSTOM_SCOPE_KEY, "autodispose2.sample.ClassWithCustomScope")
    properties.property(OVERRIDE_SCOPES, "true")
    properties.to(AutoDisposeDetector.PROPERTY_FILE)

    lint().files(
      *jars(),
      CUSTOM_SCOPE,
      properties,
      kotlin(
        """
      package autodispose2.sample
      import autodispose2.sample.ClassWithCustomScope
      import io.reactivex.rxjava3.core.Observable
      import autodispose2.ScopeProvider
      import autodispose2.autoDispose

      class MyCustomClass: ClassWithCustomScope {
        lateinit var scopeProvider: ScopeProvider
        fun doSomething() {
          val observable = Observable.just(1, 2, 3)
          observable.autoDispose(scopeProvider).subscribe()
        }
      }
    """
      ).indented()
    )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  @Test fun capturedDisposable() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable

      class MyActivity: AppCompatActivity {
        fun doSomething() {
          val disposable = Observable.just(1, 2, 3).subscribe()
        }
      }
    """
      ).indented()
    )
      .run()
      .expectClean()
  }

  @Test fun nestedDisposable() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething() {
          disposables.add(
            Observable.just(1, 2, 3).subscribe()
          )
        }
      }
    """
      ).indented()
    )
      .run()
      .expectClean()
  }

  @Test fun subscribeWithLambda() {
    lint().files(
      *jars(),
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable
      import io.reactivex.rxjava3.disposables.Disposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething() {
          Observable.just(1,2,3).subscribe {}
        }
      }
    """
      ).indented()
    )
      .run()
      .expect(
        """src/foo/MyActivity.kt:10: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    Observable.just(1,2,3).subscribe {}
          |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun checkLenientLintInIfExpression() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething(flag: Boolean) {
          if (flag) {
            Observable.just(1).subscribe()
          } else {
            Observable.just(2).subscribe()
          }
        }
      }
    """
      ).indented()
    )
      .run()
      .expect(
        """
          |src/foo/MyActivity.kt:10: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |      Observable.just(1).subscribe()
          |      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |src/foo/MyActivity.kt:12: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |      Observable.just(2).subscribe()
          |      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |2 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun checkLenientLintInIfExpressionCaptured() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething(flag: Boolean) {
          val disposable = if (flag) {
            Observable.just(1).subscribe()
          } else {
            Observable.just(2).subscribe()
          }
        }
      }
    """
      ).indented()
    )
      .run()
      .expectClean()
  }

  @Test fun checkLenientLintInWhenExpression() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething(flag: Boolean) {
          when (flag) {
            true -> Observable.just(1).subscribe()
            false -> Observable.just(2).subscribe()
          }
        }
      }
    """
      ).indented()
    )
      .run()
      .expect(
        """
          |src/foo/MyActivity.kt:10: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |      true -> Observable.just(1).subscribe()
          |              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |src/foo/MyActivity.kt:11: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |      false -> Observable.just(2).subscribe()
          |               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |2 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun checkLenientLintInWhenExpressionCaptured() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething(flag: Boolean) {
          val disposable = when (flag) {
            true -> Observable.just(1).subscribe()
            false -> Observable.just(2).subscribe()
          }
        }
      }
    """
      ).indented()
    )
      .run()
      .expectClean()
  }

  @Test fun checkLenientLintInLambdaWithUnitReturnExpression() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething() {
          val receiveReturnUnitFn: (() -> Unit) -> Unit = {}
          receiveReturnUnitFn {
            Observable.just(1).subscribe()
          }
        }
      }
    """
      ).indented()
    )
      .run()
      .expect(
        """
          |src/foo/MyActivity.kt:11: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |      Observable.just(1).subscribe()
          |      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun checkLenientLintInLambdaWithNonUnitReturnExpression() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething() {
          val receiveReturnAnyFn: (() -> Any) -> Unit = {}
          receiveReturnAnyFn {
            Observable.just(1).subscribe()
            "result"
          }
        }
      }
    """
      ).indented()
    )
      .run()
      .expect(
        """
          |src/foo/MyActivity.kt:11: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |      Observable.just(1).subscribe()
          |      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun checkLenientLintInLambdaExpressionCaptured() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      kotlin(
        """
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.rxjava3.core.Observable
      import io.reactivex.rxjava3.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething() {
          val receiveReturnAnyFn: (() -> Any) -> Unit = {}
          receiveReturnAnyFn {
            Observable.just(1).subscribe()
          }
        }
      }
    """
      ).indented()
    )
      .run()
      .expectClean()
  }

  @Test fun javaCapturedDisposable() {
    val propertiesFile = lenientPropertiesFile()
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      java(
        """
      package foo;
      import androidx.appcompat.app.AppCompatActivity;
      import io.reactivex.rxjava3.core.Observable;
      import io.reactivex.rxjava3.disposables.Disposable;

      class MyActivity extends AppCompatActivity {
        fun doSomething() {
          Disposable disposable = Observable.just(1, 2, 3).subscribe();
        }
      }
    """
      ).indented()
    )
      .run()
      .expectClean()
  }

  @Test fun javaCapturedDisposableWithoutLenientProperty() {
    val propertiesFile = lenientPropertiesFile(false)
    lint().files(
      *jars(),
      propertiesFile,
      LIFECYCLE_OWNER,
      ACTIVITY,
      java(
        """
      package foo;
      import androidx.appcompat.app.AppCompatActivity;
      import io.reactivex.rxjava3.core.Observable;
      import io.reactivex.rxjava3.disposables.Disposable;

      class MyActivity extends AppCompatActivity {
        fun doSomething() {
          Disposable disposable = Observable.just(1, 2, 3).subscribe();
        }
      }
    """
      ).indented()
    )
      .run()
      .expect(
        """src/foo/MyActivity.java:8: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
        |    Disposable disposable = Observable.just(1, 2, 3).subscribe();
        |                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun subscribeWithCapturedNonDisposableFromMethodReference() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_OWNER,
        FRAGMENT,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Observable;
          import io.reactivex.rxjava3.observers.DisposableObserver;
          import io.reactivex.rxjava3.disposables.Disposable;
          import io.reactivex.rxjava3.core.Observer;
          import androidx.fragment.app.Fragment;
          import io.reactivex.rxjava3.functions.Function;

          class ExampleClass extends Fragment {
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              try {
                Observer<Integer> observer = methodReferencable(obs::subscribeWith);
              } catch (Exception e){
              }
            }

            Observer<Integer> methodReferencable(Function<Observer<Integer>, Observer<Integer>> func) throws Exception {
              return func.apply(new Observer<Integer>() {
                @Override public void onSubscribe(Disposable d) {

                }

                @Override public void onNext(Integer integer) {

                }

                @Override public void onError(Throwable e) {

                }

                @Override public void onComplete() {

                }
              });
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.java:13: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |      Observer<Integer> observer = methodReferencable(obs::subscribeWith);
          |                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun subscribeWithCapturedDisposableFromMethodReference() {
    val propertiesFile = lenientPropertiesFile()
    lint()
      .files(
        *jars(),
        propertiesFile,
        LIFECYCLE_OWNER,
        FRAGMENT,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Observable;
          import io.reactivex.rxjava3.observers.DisposableObserver;
          import io.reactivex.rxjava3.disposables.Disposable;
          import io.reactivex.rxjava3.core.Observer;
          import androidx.fragment.app.Fragment;
          import io.reactivex.rxjava3.functions.Function;

          class ExampleClass extends Fragment {
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              try {
                Disposable disposable = methodReferencable(obs::subscribeWith);
              } catch (Exception e){
              }
            }

            DisposableObserver<Integer> methodReferencable(Function<DisposableObserver<Integer>, DisposableObserver<Integer>> func) throws Exception {
              return func.apply(new DisposableObserver<Integer>() {
                @Override
                public void onNext(Integer integer) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
              });
            }
          }
        """
        ).indented()
      )
      .run()
      .expectClean()
  }

  @Test fun kotlinSubscribeWithCapturedNonDisposableFromMethodReference() {
    val propertiesFile = lenientPropertiesFile()
    lint()
      .files(
        *jars(),
        propertiesFile,
        LIFECYCLE_OWNER,
        FRAGMENT,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Observable
          import io.reactivex.rxjava3.observers.DisposableObserver
          import io.reactivex.rxjava3.disposables.Disposable
          import io.reactivex.rxjava3.core.Observer
          import androidx.fragment.app.Fragment
          import io.reactivex.rxjava3.functions.Function

          class ExampleClass: Fragment {
            fun names() {
              val observable: Observable<Int> = Observable.just(1)
              val observer: Observer<Int> = methodReferencable(Function { observable.subscribeWith(it) })
            }

            @Throws(Exception::class)
            internal fun methodReferencable(func: Function<Observer<Int>, Observer<Int>>): Observer<Int> {
              return func.apply(object : Observer<Int> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(integer: Int) {

                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }
              })
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.kt:12: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    val observer: Observer<Int> = methodReferencable(Function { observable.subscribeWith(it) })
          |                                                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun kotlinSubscribeWithCapturedDisposableFromMethodReference() {
    val propertiesFile = lenientPropertiesFile()
    lint()
      .files(
        *jars(),
        propertiesFile,
        LIFECYCLE_OWNER,
        FRAGMENT,
        kotlin(
          """
          package foo
          import androidx.fragment.app.Fragment
          import io.reactivex.rxjava3.core.Observable
          import io.reactivex.rxjava3.core.Observer
          import io.reactivex.rxjava3.disposables.Disposable
          import io.reactivex.rxjava3.functions.Function
          import io.reactivex.rxjava3.observers.DisposableObserver

          class ExampleClass: Fragment {
            fun names() {
              val observable: Observable<Int> = Observable.just(1)
              val disposable: Disposable = methodReferencable(Function { observable.subscribeWith(it) })
            }

            @Throws(Exception::class)
            internal fun methodReferencable(func: Function<DisposableObserver<Int>, DisposableObserver<Int>>): DisposableObserver<Int> {
              return func.apply(object : DisposableObserver<Int>() {
                override fun onNext(integer: Int) {

                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }
              })
            }
          }
        """
        ).indented()
      )
      .run()
      .expectClean()
  }

  @Test fun kotlinExtensionFunctionNotConfigured() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_OWNER,
        RX_KOTLIN,
        FRAGMENT,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Observable
          import io.reactivex.rxjava3.observers.DisposableObserver
          import io.reactivex.rxjava3.kotlin.subscribeBy
          import androidx.fragment.app.Fragment

          class ExampleClass : Fragment() {
            fun names() {
              val obs = Observable.just(1, 2, 3, 4)
              obs.subscribeBy { }
            }
          }
        """
        ).indented()
      )
      .run()
      .expectClean()
  }

  @Test fun kotlinExtensionFunctionNotHandled() {
    lint()
      .files(
        *jars(),
        propertiesFile(kotlinExtensionFunctions = "io.reactivex.rxjava3.kotlin.subscribers#subscribeBy"),
        LIFECYCLE_OWNER,
        RX_KOTLIN,
        FRAGMENT,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Observable
          import io.reactivex.rxjava3.observers.DisposableObserver
          import io.reactivex.rxjava3.kotlin.subscribeBy
          import androidx.fragment.app.Fragment

          class ExampleClass : Fragment() {
            fun names() {
              val obs = Observable.just(1, 2, 3, 4)
              obs.subscribeBy { }
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.kt:10: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    obs.subscribeBy { }
          |    ~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun subscribeWithCapturedNonDisposableType() {
    lint()
      .files(
        *jars(),
        LIFECYCLE_OWNER,
        FRAGMENT,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Observable;
          import io.reactivex.rxjava3.observers.DisposableObserver;
          import io.reactivex.rxjava3.disposables.Disposable;
          import io.reactivex.rxjava3.core.Observer;
          import androidx.fragment.app.Fragment;

          class ExampleClass extends Fragment {
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              Observer<Integer> disposable = obs.subscribeWith(new Observer<Integer>() {
                @Override
                public void onSubscribe(Disposable d) {
                }
                @Override
                public void onNext(Integer integer) {
                }

                @Override
                public void onError(Throwable e) {}

                @Override
                public void onComplete() {}
              });
            }
          }
        """
        ).indented()
      )
      .run()
      .expect(
        """src/foo/ExampleClass.java:11: Error: ${AutoDisposeDetector.LINT_DESCRIPTION} [AutoDispose]
          |    Observer<Integer> disposable = obs.subscribeWith(new Observer<Integer>() {
          |                                   ^
          |1 errors, 0 warnings""".trimMargin()
      )
  }

  @Test fun subscribeWithCapturedDisposable() {
    val propertiesFile = lenientPropertiesFile()
    lint()
      .files(
        *jars(),
        propertiesFile,
        LIFECYCLE_OWNER,
        FRAGMENT,
        java(
          """
          package foo;
          import io.reactivex.rxjava3.core.Observable;
          import io.reactivex.rxjava3.observers.DisposableObserver;
          import io.reactivex.rxjava3.disposables.Disposable;
          import androidx.fragment.app.Fragment;

          class ExampleClass extends Fragment {
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              Disposable disposable = obs.subscribeWith(new DisposableObserver<Integer>() {
                @Override
                public void onNext(Integer integer) {
                }

                @Override
                public void onError(Throwable e) {}

                @Override
                public void onComplete() {}
              });
            }
          }
        """
        ).indented()
      )
      .run()
      .expectClean()
  }

  @Test fun withScope_withScopeProvider_missingAutoDispose_shouldError() {
    lint()
      .files(
        *jars(),
        SCOPE_PROVIDER,
        AUTODISPOSE_CONTEXT,
        WITH_SCOPE_PROVIDER,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Observable
          import autodispose2.ScopeProvider
          import autodispose2.autoDispose
          import autodispose2.withScope
          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val observable = Observable.just(1)
              withScope(scopeProvider) {
                observable.subscribe()
              }
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectErrorCount(1)
  }

  @Test fun withScope_withCompletable_missingAutoDispose_shouldError() {
    lint()
      .files(
        *jars(),
        AUTODISPOSE_CONTEXT,
        WITH_SCOPE_COMPLETABLE,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Completable
          import io.reactivex.rxjava3.core.Observable
          import autodispose2.withScope
          class ExampleClass {
            fun names() {
              val observable = Observable.just(1)
              withScope(Completable.complete()) {
                observable.subscribe()
              }
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectErrorCount(1)
  }

  @Test fun withScope_withScopeProvider_expectClean() {
    lint()
      .files(
        *jars(),
        SCOPE_PROVIDER,
        AUTODISPOSE_CONTEXT,
        WITH_SCOPE_PROVIDER,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Observable
          import autodispose2.ScopeProvider
          import autodispose2.autoDispose
          import autodispose2.withScope
          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val observable = Observable.just(1)
              withScope(scopeProvider) {
                observable.autoDispose().subscribe()
              }
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  @Test fun withScope_withCompletable_expectClean() {
    lint()
      .files(
        *jars(),
        AUTODISPOSE_CONTEXT,
        WITH_SCOPE_COMPLETABLE,
        kotlin(
          """
          package foo
          import io.reactivex.rxjava3.core.Completable
          import autodispose2.autoDispose
          import autodispose2.withScope
          class ExampleClass {
            fun names() {
              val observable = Observable.just(1)
              withScope(Completable.complete()) {
                observable.autoDispose().subscribe()
              }
            }
          }
        """
        ).indented()
      )
      .allowCompilationErrors() // Until AGP 7.1.0 https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
      .run()
      .expectClean()
  }

  private fun jars(): Array<TestFile> {
    val classLoader = AutoDisposeDetector::class.java.classLoader
    return arrayOf(
      LibraryReferenceTestFile(
        File(classLoader.getResource("rxjava-3.1.0.jar")!!.toURI()),
      ),
      LibraryReferenceTestFile(
        File(classLoader.getResource("autodispose-2.0.0.jar")!!.toURI())
      )
    )
  }

  override fun getDetector(): Detector {
    return AutoDisposeDetector()
  }

  override fun getIssues(): List<Issue> {
    return listOf(AutoDisposeDetector.ISSUE)
  }
}
