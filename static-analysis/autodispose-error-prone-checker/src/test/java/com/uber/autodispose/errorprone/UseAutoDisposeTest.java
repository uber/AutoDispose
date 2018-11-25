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
public class UseAutoDisposeTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private CompilationTestHelper compilationHelper;

  @Before public void setup() {
    compilationHelper = CompilationTestHelper.newInstance(UseAutoDispose.class, getClass());
  }

  @Test public void test_autodisposePositiveCasesWithDefaultClass() {
    compilationHelper.addSourceFile("UseAutoDisposeDefaultClassPositiveCases.java")
        .doTest();
  }

  @Test public void test_autodisposePositiveCaseswithCustomClass() {
    compilationHelper.setArgs(ImmutableList.of("-XepOpt:ClassesWithScope"
        + "=com.uber.autodispose.errorprone.ComponentWithLifecycle"));
    compilationHelper.addSourceFile("UseAutoDisposeCustomClassPositiveCases.java")
        .doTest();
  }

  @Test public void test_autodisposeNegativeCases() {
    compilationHelper.addSourceFile("UseAutoDisposeNegativeCases.java")
        .doTest();
  }

  @Test public void test_autodisposePositiveCasesWithDefaultClassLenient() {
    compilationHelper.setArgs(ImmutableList.of("-XepOpt:Lenient=true"));
    compilationHelper.addSourceFile("UseAutoDisposeDefaultClassPositiveCasesLenient.java")
        .doTest();
  }

  @Test public void test_autodisposeNegativeCasesLenient() {
    compilationHelper.setArgs(ImmutableList.of("-XepOpt:Lenient=true"));
    compilationHelper.addSourceFile("UseAutoDisposeNegativeCasesLenient.java")
        .doTest();
  }
}
