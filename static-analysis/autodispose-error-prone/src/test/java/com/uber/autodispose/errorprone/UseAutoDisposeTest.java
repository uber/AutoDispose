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
package com.uber.autodispose.errorprone;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class UseAutoDisposeTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private CompilationTestHelper compilationHelper;

  @Before
  public void setup() {
    compilationHelper = CompilationTestHelper.newInstance(UseAutoDispose.class, getClass());
  }

  @Test
  public void test_autodisposePositiveCasesWithDefaultClass() {
    compilationHelper.addSourceFile("UseAutoDisposeDefaultClassPositiveCases.java").doTest();
  }

  @Test
  public void test_autodisposePositiveCasesWithDefaultClassGivenCustomTypes() {
    compilationHelper
        .setArgs(
            ImmutableList.of(
                "-XepOpt:TypesWithScope"
                    + "=com.uber.autodispose.errorprone.ComponentWithLifecycle"))
        .addSourceFile("UseAutoDisposeDefaultClassPositiveCases.java")
        .doTest();
  }

  @Test
  public void test_autodisposeNegativeCasesWithDefaultClassGivenExclusiveScope() {
    compilationHelper
        .setArgs(
            ImmutableList.of(
                "-XepOpt:TypesWithScope"
                    + "=com.uber.autodispose.errorprone.ComponentWithLifecycle",
                "-XepOpt:OverrideScopes"))
        .addSourceFile("UseAutoDisposeNegativeCasesExcluded.java")
        .doTest();
  }

  @Test
  public void test_autodisposePositiveCasesWithCustomClass() {
    compilationHelper
        .setArgs(
            ImmutableList.of(
                "-XepOpt:TypesWithScope"
                    + "=com.uber.autodispose.errorprone.ComponentWithLifecycle"))
        .addSourceFile("UseAutoDisposeCustomClassPositiveCases.java")
        .doTest();
  }

  @Test
  public void test_autodisposeNegativeCases() {
    compilationHelper.addSourceFile("UseAutoDisposeNegativeCases.java").doTest();
  }

  @Test
  public void test_autodisposePositiveCasesWithDefaultClassLenient() {
    compilationHelper
        .setArgs(ImmutableList.of("-XepOpt:Lenient=true"))
        .addSourceFile("UseAutoDisposeDefaultClassPositiveCasesLenient.java")
        .doTest();
  }

  @Test
  public void test_autodisposeNegativeCasesLenient() {
    compilationHelper
        .setArgs(ImmutableList.of("-XepOpt:Lenient=true"))
        .addSourceFile("UseAutoDisposeNegativeCasesLenient.java")
        .doTest();
  }
}
