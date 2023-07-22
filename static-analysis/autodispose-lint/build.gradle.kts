import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Copyright (C) 2018. Uber Technologies
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

plugins {
  `java-library`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.mavenPublish)
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(libs.versions.lintJvmTarget.get().toInt())
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    // Lint runs with 1.6 still, so we need to emit 1.6-compatible code
    apiVersion = "1.6"
    languageVersion = "1.6"
    jvmTarget = libs.versions.lintJvmTarget.get()
  }
}

dependencies {
  kapt(libs.apt.autoService)

  compileOnly(libs.build.lint.api)
  compileOnly(libs.apt.autoService)

  testImplementation(libs.test.junit)
  testImplementation(libs.build.lint.core)
  testImplementation(libs.build.lint.tests)
}
