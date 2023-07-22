/*
 * Copyright (C) 2019. Uber Technologies
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
package autodispose2.sample;

import static autodispose2.AutoDispose.autoDisposable;

import autodispose2.ScopeProvider;
import io.reactivex.rxjava3.core.Observable;

public class ClassWithCustomScope implements CustomScope {

  void sampleCall() {
    // This class implements CustomScope, which we've informed the error prone and lint checks to
    // flag as a known type with a scope. If we comment out the autodispose line, this will fail
    // to compile or fail lint.
    Observable.just(1).to(autoDisposable(ScopeProvider.UNBOUND)).subscribe();
  }
}
