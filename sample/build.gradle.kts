/*
 * Copyright (c) 2017. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.android.build.gradle.api.BaseVariant
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.errorProne)
  alias(libs.plugins.nullAway)
}

android {
  namespace = "autodispose2.sample"
  compileSdk = libs.versions.compileSdkVersion.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdkVersion.get().toInt()
    targetSdk = libs.versions.targetSdkVersion.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testApplicationId = "autodispose2.android.lifecycle.androidTest"
    multiDexEnabled = true
  }
  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
  }
  sourceSets { getByName("main") { java.srcDirs("src/main/kotlin") } }
  lint { checkDependencies = true }
  buildTypes { getByName("debug") { matchingFallbacks += listOf("release") } }
  testOptions { execution = "ANDROIDX_TEST_ORCHESTRATOR" }

  val classesWithScope =
    listOf(
      "android.app.Activity",
      "android.app.Fragment",
      "androidx.lifecycle.LifecycleOwner",
      "autodispose2.ScopeProvider",
      "autodispose2.sample.CustomScope"
    )

  val configurer: (BaseVariant) -> Unit = { variant ->
    variant.javaCompileProvider.configure {
      options.errorprone {
        nullaway {
          severity = CheckSeverity.ERROR
          annotatedPackages.add("com.uber")
        }
        check("AutoDispose", CheckSeverity.ERROR)
        option("AutoDispose:TypesWithScope", classesWithScope.joinToString(","))
      }
    }
  }

  applicationVariants.configureEach(configurer)
  testVariants.configureEach(configurer)
  unitTestVariants.configureEach(configurer)
}

androidComponents {
  beforeVariants { builder ->
    if (builder.buildType == "release") {
      builder.enable = false
    }
  }
}

project.tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
    progressiveMode.set(true)
    jvmTarget.set(JvmTarget.JVM_1_8)
  }
}

dependencies {
  implementation(project(":android:autodispose-android"))
  implementation(project(":android:autodispose-androidx-lifecycle"))
  implementation(project(":autodispose"))
  implementation(project(":autodispose-lifecycle"))
  //  implementation project(":autodispose-rxlifecycle3")
  implementation(libs.multidex)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.material)
  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.activityKtx)
  implementation(libs.androidx.lifecycle.runtimeKtx)
  implementation(libs.androidx.lifecycle.vmKtx)
  implementation(libs.rx.android)
  implementation(libs.replaying.share.kotlin)
  implementation(libs.rxrelay)
  implementation(libs.rxjava3.bridge)

  errorprone(libs.build.errorProne)
  errorprone(libs.build.nullAway)
  errorprone(project(":static-analysis:autodispose-error-prone"))

  debugImplementation(libs.leakcanary.android)

  androidTestImplementation(project(":test-utils"))
  androidTestImplementation(libs.test.androidRunner)
  androidTestImplementation(libs.test.androidRules)
  androidTestUtil(libs.test.androidOrchestrator)
  androidTestImplementation(libs.test.androidExtJunit)
}
