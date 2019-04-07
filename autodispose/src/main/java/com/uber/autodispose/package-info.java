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

/**
 * AutoDispose is an RxJava 2 tool for automatically binding the execution of RxJava 2 streams to a
 * provided scope via disposal/cancellation.
 *
 * <p>The idea is simple: construct your chain like any other, and then at subscription you simply
 * drop in the relevant factory call + method for that type as a converter. In everyday use, it
 * usually looks like this:
 *
 * <p><code><pre>
 *   myObservable
 *     .doStuff()
 *     .as(autoDisposable(this))   // <-- AutoDispose
 *     .subscribe(s -> ...);
 * </pre></code>
 *
 * <p>By doing this, you will automatically unsubscribe from myObservable as indicated by your scope
 * - this helps prevent many classes of errors when an observable emits and item, but the actions
 * taken in the subscription are no longer valid. For instance, if a network request comes back
 * after a UI has already been torn down, the UI can't be updated - this pattern prevents this type
 * of bug.
 */
package com.uber.autodispose;
