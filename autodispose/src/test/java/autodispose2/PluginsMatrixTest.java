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
package autodispose2;

import autodispose2.AutoDisposePlugins;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class PluginsMatrixTest {

  @Parameterized.Parameters
  public static Collection primeNumbers() {
    return Arrays.asList(new Object[][] {{true}, {false}});
  }

  protected final boolean hideProxies;

  public PluginsMatrixTest(boolean hideProxies) {
    this.hideProxies = hideProxies;
  }

  @Before
  public void setUp() {
    AutoDisposePlugins.setHideProxies(hideProxies);
  }

  @After
  public void tearDown() {
    AutoDisposePlugins.reset();
  }
}
