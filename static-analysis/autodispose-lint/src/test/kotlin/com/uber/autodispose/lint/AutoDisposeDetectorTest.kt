/*
 * Copyright (C) 2018. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uber.autodispose.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestFiles.projectProperties
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class AutoDisposeDetectorTest {

  companion object {
    // Stub activity
    private val ACTIVITY = java("""
      package androidx.appcompat.app;
      import androidx.lifecycle.LifecycleOwner;

      public class AppCompatActivity implements LifecycleOwner {
      }
    """).indented()

    // Stub LifecycleOwner
    private val LIFECYCLE_OWNER = java("""
      package androidx.lifecycle;

      public interface LifecycleOwner {}
    """).indented()

    // Stub Fragment
    private val FRAGMENT = java("""
      package androidx.fragment.app;
      import androidx.lifecycle.LifecycleOwner;

      public class Fragment implements LifecycleOwner {}
    """).indented()

    // Stub Scope Provider
    private val SCOPE_PROVIDER = kotlin("""
      package com.uber.autodispose

      interface ScopeProvider
    """).indented()

    // Stub LifecycleScopeProvider
    private val LIFECYCLE_SCOPE_PROVIDER = kotlin("""
      package com.uber.autodispose.lifecycle
      import com.uber.autodispose.ScopeProvider

      interface LifecycleScopeProvider: ScopeProvider
    """).indented()

    // Custom Scope
    private val CUSTOM_SCOPE = kotlin("""
      package com.uber.autodispose.sample

      class ClassWithCustomScope {}
    """).indented()
  }

  @Test fun observableErrorsOutOnOmittingAutoDispose() {
    lint()
        .files(rxJava2(), LIFECYCLE_OWNER, FRAGMENT, java("""
          package foo;
          import io.reactivex.Observable;
          import androidx.fragment.app.Fragment;

          class ExampleClass extends Fragment {
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              obs.subscribe();
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""src/foo/ExampleClass.java:8: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    obs.subscribe();
          |    ~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun observableDisposesSubscriptionJava() {
    lint()
        .files(rxJava2(), java("""
          package foo;
          import io.reactivex.Observable;
          import com.uber.autodispose.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Observable<Integer> obs = Observable.just(1, 2, 3, 4);
              obs.as(AutoDispose.autoDisposable(scopeProvider)).subscribe();
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun observableSubscribeWithNotHandled() {
    lint()
        .files(rxJava2(), LIFECYCLE_OWNER, FRAGMENT, java("""
          package foo;
          import io.reactivex.Observable;
          import io.reactivex.observers.DisposableObserver;
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
        """).indented())
        .allowCompilationErrors(false)
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""src/foo/ExampleClass.java:9: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    obs.subscribeWith(new DisposableObserver<Integer>() {
          |    ^
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun observableSubscribeWithDisposed() {
    lint()
        .files(rxJava2(), SCOPE_PROVIDER, LIFECYCLE_OWNER, FRAGMENT, java("""
          package foo;
          import io.reactivex.Observable;
          import io.reactivex.observers.DisposableObserver;
          import androidx.fragment.app.Fragment;
          import com.uber.autodispose.ScopeProvider;

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
        """).indented())
        .allowCompilationErrors(false)
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun observableDisposesSubscriptionKotlin() {
    lint()
        .files(rxJava2(), kotlin("""
          package foo
          import io.reactivex.Observable
          import com.uber.autodispose.ScopeProvider

          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val obs = Observable.just(1, 2, 3, 4)
              obs.autoDisposable(scopeProvider).subscribe()
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun singleErrorsOutOnOmittingAutoDispose() {
    lint()
        .files(rxJava2(), LIFECYCLE_OWNER, ACTIVITY, java("""
          package foo;
          import io.reactivex.Single;
          import androidx.appcompat.app.AppCompatActivity;

          class ExampleClass extends AppCompatActivity {
            void names() {
              Single<Integer> single = Single.just(1);
              single.subscribe();
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""src/foo/ExampleClass.java:8: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    single.subscribe();
          |    ~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun singleDisposesSubscriptionJava() {
    lint()
        .files(rxJava2(), java("""
          package foo;
          import io.reactivex.Single;
          import com.uber.autodispose.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Single<Integer> single = Single.just(1);
              single.as(AutoDispose.autoDisposable(scopeProvider)).subscribe();
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun singleDisposesSubscriptionKotlin() {
    lint()
        .files(rxJava2(), kotlin("""
          package foo
          import io.reactivex.Single
          import com.uber.autodispose.ScopeProvider

          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val single = Single.just(1)
              single.autoDisposable(scopeProvider).subscribe()
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun flowableErrorsOutOnOmittingAutoDispose() {
    lint()
        .files(rxJava2(), LIFECYCLE_OWNER, java("""
          package foo;
          import io.reactivex.Flowable;
          import androidx.lifecycle.LifecycleOwner;

          class ExampleClass implements LifecycleOwner {
            void names() {
              Flowable<Integer> flowable = Flowable.just(1);
              flowable.subscribe();
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""src/foo/ExampleClass.java:8: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    flowable.subscribe();
          |    ~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun flowableDisposesSubscriptionJava() {
    lint()
        .files(rxJava2(), java("""
          package foo;
          import io.reactivex.Flowable;
          import com.uber.autodispose.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4);
              flowable.as(AutoDispose.autoDisposable(scopeProvider)).subscribe();
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun flowableDisposesSubscriptionKotlin() {
    lint()
        .files(rxJava2(), LIFECYCLE_SCOPE_PROVIDER, kotlin("""
          package foo
          import io.reactivex.Observable
          import com.uber.autodispose.lifecycle.LifecycleScopeProvider

          class ExampleClass: LifecycleScopeProvider {
            fun names() {
              val flowable = Flowable.just(1, 2, 3, 4)
              flowable.autoDisposable(this).subscribe()
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun completableErrorsOutOnOmittingAutoDispose() {
    lint()
        .files(rxJava2(), SCOPE_PROVIDER, kotlin("""
          package foo
          import io.reactivex.Completable
          import com.uber.autodispose.ScopeProvider

          class ExampleClass: ScopeProvider {
            fun names() {
              val completable = Completable.complete()
              completable.subscribe()
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""src/foo/ExampleClass.kt:8: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    completable.subscribe()
          |    ~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun completableSubscriptionNonScopedClass() {
    lint()
        .files(rxJava2(), java("""
          package foo;
          import io.reactivex.Completable;
          import com.uber.autodispose.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Completable completable = Completable.complete();
              completable.subscribe();
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun completableDisposesSubscriptionKotlin() {
    lint()
        .files(rxJava2(), kotlin("""
          package foo
          import io.reactivex.Observable
          import com.uber.autodispose.ScopeProvider

          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val completable = Completable.complete()
              completable.autoDisposable(scopeProvider).subscribe()
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun maybeErrorsOutOnOmittingAutoDispose() {
    lint()
        .files(rxJava2(), LIFECYCLE_OWNER, ACTIVITY, kotlin("""
          package foo
          import io.reactivex.Maybe
          import androidx.appcompat.app.AppCompatActivity

          class ExampleClass: AppCompatActivity {
            fun names() {
              val maybe = Maybe.just(1)
              maybe.subscribe()
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""src/foo/ExampleClass.kt:8: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    maybe.subscribe()
          |    ~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun maybeDisposesSubscriptionJava() {
    lint()
        .files(rxJava2(), java("""
          package foo;
          import io.reactivex.Maybe;
          import com.uber.autodispose.ScopeProvider;

          class ExampleClass {
            private ScopeProvider scopeProvider;
            void names() {
              Maybe<Integer> maybe = Maybe.just(1);
              maybe.as(AutoDispose.autoDisposable(scopeProvider)).subscribe();
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun maybeDisposesSubscriptionKotlin() {
    lint()
        .files(rxJava2(), kotlin("""
          package foo
          import io.reactivex.Maybe
          import com.uber.autodispose.ScopeProvider

          class ExampleClass {
            lateinit var scopeProvider: ScopeProvider
            fun names() {
              val maybe = Maybe.just(2)
              maybe.autoDisposable(scopeProvider).subscribe()
            }
          }
        """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun customScopeWithoutAutoDispose() {
    val properties = projectProperties()
    properties.property(CUSTOM_SCOPE_KEY, "com.uber.autodispose.sample.ClassWithCustomScope")
    properties.to(AutoDisposeDetector.PROPERTY_FILE)

    lint().files(rxJava2(), CUSTOM_SCOPE, properties, kotlin("""
      package com.uber.autodispose.sample
      import com.uber.autodispose.sample.ClassWithCustomScope
      import io.reactivex.Observable

      class MyCustomClass: ClassWithCustomScope {
        fun doSomething() {
          val observable = Observable.just(1, 2, 3)
          observable.subscribe()
        }
      }
    """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""
          src/com/uber/autodispose/sample/MyCustomClass.kt:8: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    observable.subscribe()
          |    ~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun customScopeWithAutoDispose() {
    val properties = projectProperties()
    properties.property(CUSTOM_SCOPE_KEY, "com.uber.autodispose.sample.ClassWithCustomScope")
    properties.to(AutoDisposeDetector.PROPERTY_FILE)

    lint().files(rxJava2(), CUSTOM_SCOPE, properties, kotlin("""
      package com.uber.autodispose.sample
      import com.uber.autodispose.sample.ClassWithCustomScope
      import io.reactivex.Observable
      import com.uber.autodispose.ScopeProvider

      class MyCustomClass: ClassWithCustomScope {
        lateinit var scopeProvider: ScopeProvider
        fun doSomething() {
          val observable = Observable.just(1, 2, 3)
          observable.autoDisposable(scopeProvider).subscribe()
        }
      }
    """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun emptyCustomScopeWithoutAutoDispose() {
    val properties = projectProperties()
    properties.to(AutoDisposeDetector.PROPERTY_FILE)

    lint().files(rxJava2(), CUSTOM_SCOPE, properties, kotlin("""
      package com.uber.autodispose.sample
      import com.uber.autodispose.sample.ClassWithCustomScope
      import io.reactivex.Observable
      import com.uber.autodispose.ScopeProvider

      class MyCustomClass: ClassWithCustomScope {
        lateinit var scopeProvider: ScopeProvider
        fun doSomething() {
          val observable = Observable.just(1, 2, 3)
          observable.subscribe() // No error since custom scope not defined in properties file.
        }
      }
    """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun capturedDisposable() {
    lint().files(rxJava2(), LIFECYCLE_OWNER, ACTIVITY, kotlin("""
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.Observable

      class MyActivity: AppCompatActivity {
        fun doSomething() {
          val disposable = Observable.just(1, 2, 3).subscribe()
        }
      }
    """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun nestedDisposable() {
    lint().files(rxJava2(), LIFECYCLE_OWNER, ACTIVITY, kotlin("""
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.Observable
      import io.reactivex.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething() {
          disposables.add(
            Observable.just(1, 2, 3).subscribe()
          )
        }
      }
    """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun subscribeWithLambda() {
    lint().files(rxJava2(), LIFECYCLE_OWNER, ACTIVITY, kotlin("""
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.Observable
      import io.reactivex.disposables.CompositeDisposable
      import io.reactive.disposables.Disposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething() {
          Observable.just(1,2,3).subscribe {}
        }
      }
    """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""src/foo/MyActivity.kt:10: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    Observable.just(1,2,3).subscribe {}
          |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun checkLenientLintWithLambdas() {
    lint().files(rxJava2(), LIFECYCLE_OWNER, ACTIVITY, kotlin("""
      package foo
      import androidx.appcompat.app.AppCompatActivity
      import io.reactivex.Observable
      import io.reactivex.disposables.CompositeDisposable

      class MyActivity: AppCompatActivity {
        private val disposables = CompositeDisposable()
        fun doSomething(list: List<String>) {
          list.map {
            Observable.just(1, 2, 3).subscribe()
          }
        }
      }
    """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun javaCapturedDisposable() {
    lint().files(rxJava2(), LIFECYCLE_OWNER, ACTIVITY, java("""
      package foo;
      import androidx.appcompat.app.AppCompatActivity;
      import io.reactivex.Observable;
      import io.reactivex.disposables.Disposable;

      class MyActivity extends AppCompatActivity {
        fun doSomething() {
          Disposable disposable = Observable.just(1, 2, 3).subscribe();
        }
      }
    """).indented())
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test fun subscribeWithCapturedNonDisposableType() {
    lint()
        .files(rxJava2(), LIFECYCLE_OWNER, FRAGMENT, java("""
          package foo;
          import io.reactivex.Observable;
          import io.reactivex.observers.DisposableObserver;
          import io.reactivex.disposables.Disposable;
          import io.reactivex.Observer;
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
        """).indented())
        .allowCompilationErrors(false)
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expect("""src/foo/ExampleClass.java:11: Error: Always apply an AutoDispose scope before subscribing within defined scoped elements. [AutoDisposeUsage]
          |    Observer<Integer> disposable = obs.subscribeWith(new Observer<Integer>() {
          |                                   ^
          |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun subscribeWithCapturedDisposable() {
    lint()
        .files(rxJava2(), LIFECYCLE_OWNER, FRAGMENT, java("""
          package foo;
          import io.reactivex.Observable;
          import io.reactivex.observers.DisposableObserver;
          import io.reactivex.disposables.Disposable;
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
        """).indented())
        .allowCompilationErrors(false)
        .issues(AutoDisposeDetector.ISSUE)
        .run()
        .expectClean()
  }
}
