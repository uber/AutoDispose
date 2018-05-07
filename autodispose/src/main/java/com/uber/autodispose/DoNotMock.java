package com.uber.autodispose;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a given type should not be mocked. This is a copy of what was in
 * Error-Prone's annotations artifact before it was removed, but left for documentation purposes.
 * <p>
 * This has been modified to have CLASS retention and is only applicable to TYPE targets.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@interface DoNotMock {
  /**
   * The reason why the annotated type should not be mocked.
   *
   * <p>This should suggest alternative APIs to use for testing objects of this type.
   */
  String value() default "Create a real instance instead";
}
