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

import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import org.junit.Test;

public class TestScopeProviderTest {

  private final TestObserver<Object> o = new TestObserver<>();

  @Test
  public void noArgs() {
    TestScopeProvider provider = TestScopeProvider.create();
    provider.requestScope().subscribe(o);

    provider.emit();
    o.assertComplete();
  }

  @Test
  public void delegateArg() {
    CompletableSubject s = CompletableSubject.create();
    TestScopeProvider provider = TestScopeProvider.create(s);
    provider.requestScope().subscribe(o);

    provider.emit();
    o.assertComplete();
  }

  @Test
  public void delegateArgEmits() {
    CompletableSubject s = CompletableSubject.create();
    TestScopeProvider provider = TestScopeProvider.create(s);
    provider.requestScope().subscribe(o);

    s.onComplete();
    o.assertComplete();
  }

  @Test
  public void delegateArg_error() {
    CompletableSubject s = CompletableSubject.create();
    TestScopeProvider provider = TestScopeProvider.create(s);
    provider.requestScope().subscribe(o);

    s.onError(new IllegalArgumentException());
    o.assertError(IllegalArgumentException.class);
  }
}
