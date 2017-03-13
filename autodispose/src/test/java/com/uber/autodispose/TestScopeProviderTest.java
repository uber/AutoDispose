/*
 * Copyright (c) 2017. Uber Technologies
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

package com.uber.autodispose;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.MaybeSubject;
import org.junit.Test;

public class TestScopeProviderTest {

  private final TestObserver<Object> o = new TestObserver<>();

  @Test public void noArgs() {
    TestScopeProvider provider = TestScopeProvider.create();
    provider.requestScope()
        .subscribe(o);

    provider.emit();
    o.assertValueCount(1);
  }

  @Test public void delegateArg() {
    MaybeSubject<Integer> s = MaybeSubject.create();
    TestScopeProvider provider = TestScopeProvider.create(s);
    provider.requestScope()
        .subscribe(o);

    provider.emit();
    o.assertValueCount(1);
  }

  @Test public void delegateArgEmits() {
    MaybeSubject<Integer> s = MaybeSubject.create();
    TestScopeProvider provider = TestScopeProvider.create(s);
    provider.requestScope()
        .subscribe(o);

    s.onSuccess(1);
    o.assertValueCount(1);
    o.assertValue(1);
  }

  @Test public void delegateArg_error() {
    MaybeSubject<Integer> s = MaybeSubject.create();
    TestScopeProvider provider = TestScopeProvider.create(s);
    provider.requestScope()
        .subscribe(o);

    s.onError(new IllegalArgumentException());
    o.assertError(IllegalArgumentException.class);
  }

  @Test public void noArgs_complete() {
    TestScopeProvider provider = TestScopeProvider.create();
    provider.requestScope()
        .subscribe(o);

    provider.emitComplete();
    o.assertComplete();
  }

  @Test public void delegateArg_complete() {
    MaybeSubject<Integer> s = MaybeSubject.create();
    TestScopeProvider provider = TestScopeProvider.create(s);
    provider.requestScope()
        .subscribe(o);

    s.onComplete();
    o.assertComplete();
  }

  @Test public void delegateArg_providerComplete() {
    MaybeSubject<Integer> s = MaybeSubject.create();
    TestScopeProvider provider = TestScopeProvider.create(s);
    provider.requestScope()
        .subscribe(o);

    provider.emitComplete();
    o.assertComplete();
  }
}
