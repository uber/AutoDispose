/*
 * Copyright (C) 2017. Uber Technologies
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

plugins {
  alias(libs.plugins.mavenPublish)
}

android {
  namespace = "autodispose2.android"
}

dependencies {
  api(project(":autodispose"))
  api(libs.rx.java)
  api(libs.androidx.annotations)
  implementation(libs.rx.android)

  lintPublish(project(":static-analysis:autodispose-lint"))

  testImplementation(project(":test-utils"))
  testImplementation(libs.test.junit)
  testImplementation(libs.test.truth)
  androidTestImplementation(project(":test-utils"))
  androidTestImplementation(libs.androidx.annotations)
  androidTestImplementation(libs.test.androidExtJunit)
  androidTestImplementation(libs.test.androidRunner)
  androidTestImplementation(libs.test.androidRules)
  androidTestUtil(libs.test.androidOrchestrator)
}
