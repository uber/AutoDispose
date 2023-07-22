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
@file:Suppress("NOTHING_TO_INLINE")

package autodispose2.androidx.lifecycle.test

import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleRegistry
import io.reactivex.rxjava3.annotations.CheckReturnValue

/** Extension that returns a [TestLifecycleOwner] for this [LifecycleRegistry]. */
@Suppress("DEPRECATION")
@SuppressLint("RestrictedApi")
@CheckReturnValue
@Deprecated("Switch to androidx.lifecycle.testing.TestLifecycleOwner")
inline fun LifecycleRegistry.test(): TestLifecycleOwner = TestLifecycleOwner.create(this)
