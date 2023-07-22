/*
 * Copyright (C) 2017. Uber Technologies
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
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import net.ltgt.gradle.errorprone.CheckSeverity
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import java.net.URI

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.kapt) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.errorProne) apply false
  alias(libs.plugins.nullAway) apply false
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.animalSniffer) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.binaryCompatibilityValidator)
}

apiValidation {
  ignoredProjects += listOf("sample", "test-utils")
}

val mixedSourcesArtifacts = setOf(
    "autodispose",
    "autodispose-android",
    "autodispose-androidx-lifecycle",
    "autodispose-androidx-lifecycle-test",
    "autodispose-lifecycle"
)
// These are files with different copyright headers that should not be modified automatically.
val copiedFiles = listOf(
    "AtomicThrowable",
    "AutoDisposableHelper",
    "AutoDisposeBackpressureHelper",
    "AutoDisposeEndConsumerHelper",
    "AutoSubscriptionHelper",
    "ExceptionHelper",
    "HalfSerializer",
).map { "**/*${it}.java" }.toTypedArray()

val compileSdkVersionInt: Int = libs.versions.compileSdkVersion.get().toInt()
val targetSdkVersion: Int = libs.versions.targetSdkVersion.get().toInt()
val minSdkVersion: Int = libs.versions.minSdkVersion.get().toInt()
val ktLintVersion = libs.versions.ktlint.get()
val jvmTargetString = libs.versions.jvmTarget.get()
val lintJvmTargetString = libs.versions.lintJvmTarget.get()
val nullAwayDep = libs.build.nullAway
val errorProneDep = libs.build.errorProne
subprojects {
//  apply(plugin = "com.diffplug.spotless")
//  configure<SpotlessExtension> {
//    format "misc", {
//      target "**/*.md", "**/.gitignore"
//
//      indentWithTabs()
//      trimTrailingWhitespace()
//      endWithNewline()
//    }
//    kotlin {
//      target "**/*.kt"
//      ktlint(libs.versions.ktlint.get()).userData(["indent_size": "2", "continuation_indent_size" : "2"])
//      licenseHeaderFile rootProject.file("spotless/copyright.kt")
//      trimTrailingWhitespace()
//      endWithNewline()
//    }
//    java {
//      target "**/*.java"
//      targetExclude(copiedFiles)
//      googleJavaFormat(libs.versions.gjf.get())
//      licenseHeaderFile rootProject.file("spotless/copyright.java")
//      removeUnusedImports()
//      trimTrailingWhitespace()
//      endWithNewline()
//    }
//    groovyGradle {
//      target "**/*.gradle"
//      trimTrailingWhitespace()
//      endWithNewline()
//    }
//  }

  val isMixedSourceSet = project.name in mixedSourcesArtifacts
  val isAndroidLibrary = project.path.startsWith(":android:")
  val isLint = project.path.endsWith("-lint")
  val isKotlin = project.path.endsWith("-ktx") || isLint || isMixedSourceSet || project.path.contains("coroutines")
  val isSample = project.name == "sample"
  val isJavaLibrary = !isAndroidLibrary && !isKotlin && !isSample || (isMixedSourceSet && !isAndroidLibrary)
  val usesErrorProne = !isKotlin && !isSample || isMixedSourceSet
  project.pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
      }
    }
  }
  if (isAndroidLibrary) {
    project.apply(plugin = "com.android.library")
    project.configure<LibraryExtension> {
      compileSdk = compileSdkVersionInt

      defaultConfig {
        minSdk = minSdkVersion
        consumerProguardFiles("consumer-proguard-rules.txt")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testApplicationId = "autodispose2.android.lifecycle.androidTest"
      }
      compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmTargetString)
        targetCompatibility = JavaVersion.toVersion(jvmTargetString)
      }
      lint {
        lintConfig = file("lint.xml")
      }
      testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
      }
    }
  } else if (!isSample && !isLint) {
    project.tasks.withType<JavaCompile>().configureEach {
      // Cannot set JavaCompile's release flag in android projects
      options.release.set(8)
    }
  }
  if (isKotlin) {
    if (isAndroidLibrary) {
      project.apply(plugin = "org.jetbrains.kotlin.android")
      project.apply(plugin = "org.jetbrains.dokka")

      project.tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
          freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-progressive"
          )
          jvmTarget = jvmTargetString
        }
      }

      project.configure<KotlinProjectExtension> {
        explicitApi()
      }
    } else {
      project.apply(plugin = "org.jetbrains.kotlin.jvm")
      project.apply(plugin = "org.jetbrains.dokka")

      project.tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
          freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-progressive"
          )
          jvmTarget = if (isLint) {
            lintJvmTargetString
          } else {
            jvmTargetString
          }
        }
      }
      project.configure<KotlinProjectExtension> {
        explicitApi()
      }
    }
    project.pluginManager.withPlugin("org.jetbrains.dokka") {
      tasks.withType<DokkaTaskPartial>() {
        outputDirectory.set(rootProject.file("docs/2.x"))
        moduleName.set(project.providers.gradleProperty("POM_ARTIFACT_ID"))
        moduleVersion.set(project.providers.gradleProperty("VERSION_NAME"))
        dokkaSourceSets.configureEach {
          skipDeprecated.set(true)
          includes.from("Module.md")
          suppressGeneratedFiles.set(true)
          suppressInheritedMembers.set(true)
          externalDocumentationLink {
            url.set(URI("http://reactivex.io/RxJava/3.x/javadoc/").toURL())
          }
          externalDocumentationLink {
            url.set(URI("https://kotlin.github.io/kotlinx.coroutines/index.html").toURL())
          }
          perPackageOption {
            matchingRegex.set("/.*\\.internal.*/")
            suppress.set(true)
          }
        }
      }
    }
  }
  if (isJavaLibrary) {
    project.apply(plugin = "java-library")
    project.tasks.withType<Test>().configureEach {
      testLogging.showStandardStreams = true
    }
  }
  if (usesErrorProne) {
    project.apply(plugin = "net.ltgt.errorprone")
    project.apply(plugin = "net.ltgt.nullaway")
    project.dependencies {
      add("errorprone", nullAwayDep)
      add("errorprone", errorProneDep)
    }
    if (isJavaLibrary) {
      project.tasks.withType<JavaCompile>().configureEach {
        options.errorprone.nullaway {
          severity = CheckSeverity.ERROR
          annotatedPackages.add("autodispose2")
        }
      }
    }
  }
  if (isAndroidLibrary) {
    configure<LibraryAndroidComponentsExtension> {
      beforeVariants(selector().withBuildType("debug")) { builder ->
        builder.enable = false
      }
    }
  }
  afterEvaluate {
    if (isAndroidLibrary && usesErrorProne) {
      configure<LibraryExtension> {
        @Suppress("DEPRECATION") // no alternative
        val configurer: (BaseVariant) -> Unit = { variant ->
          variant.javaCompileProvider.configure {
            options.errorprone.nullaway {
              severity = CheckSeverity.ERROR
              annotatedPackages.add("autodispose2")
            }
          }
        }
        libraryVariants.configureEach(configurer)
        testVariants.configureEach(configurer)
        unitTestVariants.configureEach(configurer)
      }
    }
  }
}

tasks.register<Delete>("clean") {
  delete(rootProject.buildDir)
}
