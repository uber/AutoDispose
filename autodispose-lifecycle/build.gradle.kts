/*
 * Copyright (C) 2018. Uber Technologies
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
plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.animalSniffer)
  alias(libs.plugins.mavenPublish)
}

dependencies {
  api(project(":autodispose"))
  compileOnly(libs.build.errorProneAnnotations)

  signature(libs.build.animalSniffer) {
    artifact {
      name = "java17"
      type = "signature"
    }
  }

  testImplementation(project(":test-utils"))
}
