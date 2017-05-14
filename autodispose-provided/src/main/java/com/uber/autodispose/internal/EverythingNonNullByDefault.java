package com.uber.autodispose.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * This annotation can be applied to a package, class or method to indicate that the class fields,
 * method return types and parameters in that element are not null by default unless:
 * - The method overrides a method in a superclass (in which case the annotation of the
 * corresponding parameter in the superclass applies).
 * - There is a default parameter annotation applied to a more tightly nested element.
 */
@Documented
@Nonnull
@TypeQualifierDefault({
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface EverythingNonNullByDefault { }
