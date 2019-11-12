#!/bin/bash

#
# Copyright (C) 2019. Zac Sweers
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# This is a script to verify the local gradle-wrapper.jar's checksum
# matches the corresponding one from gradle for that particular version.
# Best place to run this is on CI as a pre-build step, and can help
# guard OSS projects from potentially harmful custom jars in external
# contributions.
#
# Original script https://gist.github.com/ZacSweers/91597b19744f5fe314313ae9b11d6879
#
# It expects to be run from the root project directory, with
# gradle-wrapper.properties and gradle-wrapper.jar located in the
# conventional relative "gradle/wrapper" subdirectory of the root project
# directory. It expects the distribution to be "all" and not "bin".
#
# - <rootProjectDir>
#    - gradle
#      - wrapper
#        | gradle-wrapper.jar
#        | gradle-wrapper.properties
#
# My bash-fu isn't great and I'm sure this could be improved. Suggestions welcome in the comments!
#

# Make sure we have sha256sum
if ! [ -x "$(command -v sha256sum)" ]; then
  echo 'Error: sha256sum is not installed.' >&2
  exit 1
fi

# First parse the gradle version from its gradle-wrapper.properties file
GRADLE_WRAPPER_PROPERTIES_FILE=gradle/wrapper/gradle-wrapper.properties
GRADLE_URL_PREFIX="https\://services.gradle.org/distributions/gradle-"
GRADLE_URL_SUFFIX="-all.zip"

function prop {
    grep "${1}" ${GRADLE_WRAPPER_PROPERTIES_FILE}|cut -d'=' -f2
}

# Get the full string - "https://services.gradle.org/distributions/gradle-5.6-all.zip"
GRADLE_VERSION_URL=$(prop "distributionUrl")
# Chop the prefix off - "5.6-all.zip"
GRADLE_VERSION_STRIPPED_PREFIX=${GRADLE_VERSION_URL#"$GRADLE_URL_PREFIX"}
# Chop the suffix off - "5.6"
GRADLE_VERSION=${GRADLE_VERSION_STRIPPED_PREFIX%"$GRADLE_URL_SUFFIX"}

# Now compare against gradle's distribution upstream with sha256sum
echo "Checking Gradle wrapper jar for version: ${GRADLE_VERSION}"
cd gradle/wrapper

# Download gradle's checksum for this version
# Guidance from https://docs.gradle.org/current/userguide/gradle_wrapper.html#wrapper_checksum_verification
curl --location --output gradle-wrapper.jar.sha256 \
       https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-wrapper.jar.sha256
echo "  gradle-wrapper.jar" >> gradle-wrapper.jar.sha256
sha256sum --check gradle-wrapper.jar.sha256
if [[ $? != 0 ]]; then
    echo "Gradle wrapper failed checksum verification. Please investigate." >&2
    # We leave the gradle-wrapper.jar.sha256 file around in case they want to check it
    exit $?
fi
rm gradle-wrapper.jar.sha256
cd ../..