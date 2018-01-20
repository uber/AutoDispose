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

package com.uber.autodispose.error.prone.checker;

import com.google.errorprone.CompilationTestHelper;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AutoDisposeLeakCheckerTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private CompilationTestHelper compilationHelper;

  @Before
  public void setup() {
    compilationHelper =
        CompilationTestHelper
            .newInstance(AutoDisposeLeakChecker.class, getClass());
  }

  @Test
  public void test_autodisposePositiveCasesWithDefaultClass() {
    compilationHelper
        .addSourceFile("AutoDisposeLeakCheckerDefaultClassPositiveCases.java")
        .doTest();
  }

  @Test
  public void test_autodisposePositiveCaseswithCustomClass() {
    compilationHelper.setArgs(
        Collections.singletonList("-XepOpt:AutoDisposeLeakCheck"
            + "=com.uber.autodispose.error.prone.checker.ComponentWithLifeCycle"));
    compilationHelper
        .addSourceFile("AutoDisposeLeakCheckerCustomClassPositiveCases.java")
        .doTest();
  }

  @Test
  public void test_autodisposeNegativeCases() {
    compilationHelper
        .addSourceFile("AutoDisposeLeakCheckerNegativeCases.java")
        .doTest();
  }
}
