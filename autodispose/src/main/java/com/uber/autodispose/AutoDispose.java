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

package com.uber.autodispose;

import io.reactivex.Maybe;

/**
 * Entry points for creating auto-disposing observers.
 * <p>
 * The top level entry points simply correspond to the RxJava type and serve as entry points to the
 * fluent AutoDispose API for that type. The primary points are:
 * <ul>
 * </ul>
 * <p>
 * The basic flow stencil might look like this:
 * <pre><code>
 *   myObservable.subscribe(AutoDispose.observable()
 *        .scopeWith(...)
 *        .around(...))
 * </code></pre>
 * <p>
 * There are several overloads for scopeWith(), with the most basic being a simple {@link Maybe}.
 * This {@link Maybe} is ultimately what every scope resolves to under the hood, and AutoDispose has
 * some built in understanding for predefined types. The scope is considered ended upon onSuccess
 * emission of this {@link Maybe}. The most common use case would probably be {@link ScopeProvider}.
 * <p>
 * The second part of AutoDisposal is composing your actual emission logic. Each scope overload
 * returns a subscribe clause that simply mirrors the actual type's top-level subscribe() overloads.
 */
public final class AutoDispose {

}
