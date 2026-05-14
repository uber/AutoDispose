/*
 * Copyright (C) 2017. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.spotless.LineEnding
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.errorProne) apply false
  alias(libs.plugins.nullAway) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.animalSniffer) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.spotless)
  alias(libs.plugins.binaryCompatibilityValidator)
}

apiValidation { ignoredProjects += listOf("sample", "test-utils") }

val mixedSourcesArtifacts =
  setOf(
    "autodispose",
    "autodispose-android",
    "autodispose-androidx-lifecycle",
    "autodispose-androidx-lifecycle-test",
    "autodispose-lifecycle",
  )
// These are files with different copyright headers that should not be modified automatically.
val copiedFiles =
  listOf(
      "AtomicThrowable",
      "AutoDisposableHelper",
      "AutoDisposeBackpressureHelper",
      "AutoDisposeEndConsumerHelper",
      "AutoSubscriptionHelper",
      "ExceptionHelper",
      "HalfSerializer",
    )
    .map { "**/*${it}.java" }
    .toTypedArray()

dokka {
  dokkaPublications.html {
    outputDirectory.set(rootDir.resolve("docs/api/2.x"))
    includes.from(project.layout.projectDirectory.file("README.md"))
  }
}

val ktfmtVersion = libs.versions.ktfmt.get()

allprojects {
  apply(plugin = "com.diffplug.spotless")
  val spotlessFormatters: SpotlessExtension.() -> Unit = {
    lineEndings = LineEnding.PLATFORM_NATIVE

    format("misc") {
      target("**/*.md", "**/.gitignore")
      leadingTabsToSpaces(2)
      trimTrailingWhitespace()
      endWithNewline()
    }
    kotlin {
      target("**/src/**/*.kt")
      targetExclude("spotless/copyright.kt")
      ktfmt(ktfmtVersion).googleStyle()
      licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
      trimTrailingWhitespace()
      endWithNewline()
    }
    kotlinGradle {
      target("*.kts")
      targetExclude("spotless/copyright.kt")
      ktfmt(ktfmtVersion).googleStyle()
      trimTrailingWhitespace()
      endWithNewline()
      licenseHeaderFile(
        rootProject.file("spotless/copyright.kt"),
        "(import|plugins|buildscript|dependencies|pluginManagement|dependencyResolutionManagement)",
      )
    }

    java {
      target("**/*.java")
      targetExclude(*copiedFiles, "spotless/copyright.java")
      googleJavaFormat(libs.versions.gjf.get())
      licenseHeaderFile(rootProject.file("spotless/copyright.java"))
      removeUnusedImports()
      trimTrailingWhitespace()
      endWithNewline()
    }
  }
  configure<SpotlessExtension> {
    spotlessFormatters()
    if (project.rootProject == project) {
      predeclareDeps()
    }
  }
  if (project.rootProject == project) {
    configure<SpotlessExtensionPredeclare> { spotlessFormatters() }
  }
}

val compileSdkVersionInt: Int = libs.versions.compileSdkVersion.get().toInt()
val targetSdkVersion: Int = libs.versions.targetSdkVersion.get().toInt()
val minSdkVersion: Int = libs.versions.minSdkVersion.get().toInt()
val defaultJvmTargetVersion = libs.versions.jvmTarget
val lintJvmTargetVersion = libs.versions.lintJvmTarget
val nullAwayDep = libs.build.nullAway
val errorProneDep = libs.build.errorProne

subprojects {
  val isSample = project.name == "sample"
  val isLint = project.path.contains("static-analysis")
  val isAndroid = project.path.startsWith(":android:") || isSample
  val jvmTargetVersion =
    if (isLint) {
      lintJvmTargetVersion
    } else {
      defaultJvmTargetVersion
    }

  pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(
          JavaLanguageVersion.of(libs.versions.jdk.get().removeSuffix("-ea").toInt())
        )
      }
    }

    if (!isAndroid && !isLint) {
      tasks.withType<JavaCompile>().configureEach {
        options.release.set(jvmTargetVersion.map { it.removePrefix("1.") }.map(String::toInt))
      }
    }
  }

  val configureKotlin =
    Action<AppliedPlugin> {
      configure<KotlinProjectExtension> {
        if (!isSample) {
          explicitApi()
        }
        val jvmCompilerOptions: KotlinJvmCompilerOptions.() -> Unit = {
          jvmTarget.set(jvmTargetVersion.map(JvmTarget::fromTarget))
          freeCompilerArgs.addAll("-Xjsr305=strict")
          if (!isLint) {
            progressiveMode.set(true)
          }
        }
        when (this) {
          is KotlinJvmProjectExtension -> compilerOptions(jvmCompilerOptions)
          is KotlinAndroidProjectExtension -> compilerOptions(jvmCompilerOptions)
        }
      }
    }
  pluginManager.withPlugin("org.jetbrains.kotlin.jvm", configureKotlin)
  pluginManager.withPlugin("org.jetbrains.kotlin.android", configureKotlin)

  pluginManager.withPlugin("com.vanniktech.maven.publish") {
    project.apply(plugin = "org.jetbrains.dokka")

    configure<DokkaExtension> {
      moduleName.set(project.property("POM_ARTIFACT_ID").toString())
      moduleVersion.set(project.property("VERSION_NAME").toString())
      basePublicationsDirectory.set(layout.buildDirectory.dir("dokkaDir"))
      dokkaPublications.configureEach { suppressInheritedMembers.set(true) }
      dokkaSourceSets.configureEach {
        val readMeProvider = project.layout.projectDirectory.file("README.md")
        if (readMeProvider.asFile.exists()) {
          includes.from(readMeProvider)
        }
        suppressGeneratedFiles.set(true)

        if (name.contains("androidTest", ignoreCase = true)) {
          suppress.set(true)
        }
        skipDeprecated.set(true)
        documentedVisibilities.add(VisibilityModifier.Public)

        // Skip internal packages
        perPackageOption {
          // language=RegExp
          matchingRegex.set(".*\\.internal\\..*")
          suppress.set(true)
        }
        // AndroidX and Android docs are automatically added by the Dokka plugin.
        externalDocumentationLinks.apply {
          create("RxJava").apply { url("https://reactivex.io/RxJava/3.x/javadoc/") }
          create("Coroutines").apply {
            url("https://kotlin.github.io/kotlinx.coroutines/index.html")
          }
        }

        // Add source links
        sourceLink {
          localDirectory.set(layout.projectDirectory.dir("src"))
          val relPath = rootProject.projectDir.toPath().relativize(projectDir.toPath())
          remoteUrl(
            providers.gradleProperty("POM_SCM_URL").map { scmUrl ->
              "$scmUrl/tree/main/$relPath/src"
            }
          )
          remoteLineSuffix.set("#L")
        }
      }
    }

    configure<MavenPublishBaseExtension> {
      publishToMavenCentral(automaticRelease = true)
      signAllPublications()
    }
  }

  // Common android config
  val commonAndroidConfig: CommonExtension<*, *, *, *, *, *>.() -> Unit = {
    compileSdk = compileSdkVersionInt

    defaultConfig {
      minSdk = minSdkVersion
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      testApplicationId = "autodispose2.androidTest"
    }
    compileOptions {
      sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion.get())
      targetCompatibility = JavaVersion.toVersion(jvmTargetVersion.get())
    }
    lint {
      checkTestSources = true
      val lintXml = file("lint.xml")
      if (lintXml.exists()) {
        lintConfig = lintXml
      }
    }
    testOptions { execution = "ANDROIDX_TEST_ORCHESTRATOR" }
  }

  pluginManager.withPlugin("com.android.library") {
    project.configure<LibraryExtension> {
      commonAndroidConfig()
      defaultConfig { consumerProguardFiles("consumer-proguard-rules.txt") }
      testBuildType = "release"
      configure<LibraryAndroidComponentsExtension> {
        beforeVariants(selector().withBuildType("debug")) { builder -> builder.enable = false }
      }
    }
  }

  pluginManager.withPlugin("com.android.application") {
    project.configure<ApplicationExtension> {
      commonAndroidConfig()
      configure<ApplicationAndroidComponentsExtension> {
        // Only debug enabled for this one
        beforeVariants { builder ->
          builder.enable = builder.buildType != "release"
          builder.enableAndroidTest = false
          builder.enableUnitTest = false
        }
      }
    }
  }

  project.apply(plugin = "net.ltgt.errorprone")
  project.apply(plugin = "net.ltgt.nullaway")
  project.dependencies {
    add("errorprone", nullAwayDep)
    add("errorprone", errorProneDep)
  }
  project.tasks.withType<JavaCompile>().configureEach {
    options.errorprone.nullaway {
      severity = CheckSeverity.ERROR
      annotatedPackages.add("autodispose2")
    }
  }
}
