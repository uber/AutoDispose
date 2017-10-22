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
 * Interop for RxLifecycle. It could understand RxLifecycle's
 * LifecycleProvider mechanics, which in turn lend support to different
 * components such as RxActivity. It does so by providing converters to retrieve ScopeProvider.
 */
@com.uber.javaxextras.FieldsMethodsAndParametersAreNonNullByDefault
package com.ubercab.autodispose.rxlifecycle;

