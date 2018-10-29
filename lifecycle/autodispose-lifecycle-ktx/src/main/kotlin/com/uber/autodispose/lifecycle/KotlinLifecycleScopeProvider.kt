/*
 * Copyright (C) 2018. Uber Technologies
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

package com.uber.autodispose.lifecycle

import kotlin.DeprecationLevel.HIDDEN

/**
 * A convenience [LifecycleScopeProvider] that has a default implementation for
 * [requestScope].
 *
 * @param <E> the lifecycle event type.
 */
@Deprecated(
    message = "This functionality is now pushed up into LifecycleScopeProvider directly.",
    replaceWith = ReplaceWith(
        expression = "LifecycleScopeProvider<FIXME>", // Unfortunately we can't make this better yet https://youtrack.jetbrains.com/issue/KT-21195
        imports = ["com.uber.autodispose.lifecycle.LifecycleScopeProvider"]
    ),
    level = HIDDEN
)
interface KotlinLifecycleScopeProvider<E> : LifecycleScopeProvider<E>
