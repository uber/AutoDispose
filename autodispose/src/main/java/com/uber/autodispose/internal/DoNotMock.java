/*
 * Copyright 2019. Uber Technologies
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
package com.uber.autodispose.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a given type should not be mocked. This is a copy of what was in
 * Error-Prone's annotations artifact before it was removed, but left for documentation purposes.
 *
 * <p>This has been modified to have CLASS retention and is only applicable to TYPE targets.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface DoNotMock {
  /**
   * The reason why the annotated type should not be mocked.
   *
   * <p>This should suggest alternative APIs to use for testing objects of this type.
   */
  String value() default "Create a real instance instead";
}
