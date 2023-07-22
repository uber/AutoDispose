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
  alias(libs.plugins.ksp)
}

dependencies {
  ksp(libs.autoService.ksp)

  compileOnly(libs.autoService.annotations)
  compileOnly(libs.build.errorProneCheckApi)

  testImplementation(libs.build.errorProneTestHelpers) {
    exclude(group = "junit", module = "junit")
  }
  testImplementation(libs.rx.java)
  testImplementation(libs.test.junit)
  testImplementation(project(":autodispose"))
  testImplementation(project(":autodispose-lifecycle"))
}
