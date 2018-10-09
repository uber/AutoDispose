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

@file:Suppress("NOTHING_TO_INLINE")

package com.uber.autodispose.android.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleOwner
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import io.reactivex.annotations.CheckReturnValue

/**
 * Extension that returns a [ScopeProvider] for this [LifecycleOwner].
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(): ScopeProvider = AndroidLifecycleScopeProvider.from(
    this)

/**
 * Extension that returns a [ScopeProvider] for this [LifecycleOwner].
 *
 * @param untilEvent the event until the scope is valid.
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(
    untilEvent: Lifecycle.Event): ScopeProvider = AndroidLifecycleScopeProvider.from(
    this, untilEvent)

/**
 * Extension that returns a [ScopeProvider] for this [LifecycleOwner].
 *
 * @param boundaryResolver function that resolves the event boundary.
 */
@CheckReturnValue
inline fun LifecycleOwner.scope(
    boundaryResolver: CorrespondingEventsFunction<Event>): ScopeProvider = AndroidLifecycleScopeProvider.from(
    this, boundaryResolver)

/**
 * Extension that returns a [ScopeProvider] for this [Lifecycle].
 */
@CheckReturnValue
inline fun Lifecycle.scope(): ScopeProvider = AndroidLifecycleScopeProvider.from(this)

/**
 * Extension that returns a [ScopeProvider] for this [Lifecycle].
 *
 * @param untilEvent the event until the scope is valid.
 */
@CheckReturnValue
inline fun Lifecycle.scope(
    untilEvent: Lifecycle.Event): ScopeProvider = AndroidLifecycleScopeProvider.from(
    this, untilEvent)

/**
 * Extension that returns a [ScopeProvider] for this [Lifecycle].
 *
 * @param boundaryResolver function that resolves the event boundary.
 */
@CheckReturnValue
inline fun Lifecycle.scope(
    boundaryResolver: CorrespondingEventsFunction<Event>): ScopeProvider = AndroidLifecycleScopeProvider.from(
    this, boundaryResolver)
