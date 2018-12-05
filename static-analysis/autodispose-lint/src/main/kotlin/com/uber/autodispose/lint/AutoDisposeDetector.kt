/*
 * Copyright (C) 2018. Uber Technologies
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

package com.uber.autodispose.lint

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Context
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UClassInitializer
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.getContainingUClass
import java.io.StringReader
import java.util.Properties
import java.util.EnumSet

internal const val CUSTOM_SCOPE_KEY = "autodispose.typesWithScope"

/**
 * Detector which checks if your stream subscriptions are handled by AutoDispose.
 */
class AutoDisposeDetector: Detector(), SourceCodeScanner {

  companion object {
    private const val LINT_DESCRIPTION = "Always apply an AutoDispose scope before subscribing " +
        "within defined scoped elements."

    val ISSUE: Issue = Issue.create(
        "AutoDisposeUsage",
        LINT_DESCRIPTION,
        "You're subscribing to an observable but not handling it's subscription. This "
            + "can result in memory leaks. You can avoid memory leaks by appending " +
            "`.as(autoDisposable(this))` before you subscribe.",
        Category.CORRECTNESS,
        10,
        Severity.ERROR,
        // We use the overloaded constructor that takes a varargs of `Scope` as the last param.
        // This is to enable on-the-fly IDE checks. We are telling lint to run on both
        // JAVA and TEST_SOURCES in the `scope` parameter but by providing the `analysisScopes`
        // params, we're indicating that this check can run on either JAVA or TEST_SOURCES and
        // doesn't require both of them together.
        // From discussion on lint-dev https://groups.google.com/d/msg/lint-dev/ULQMzW1ZlP0/1dG4Vj3-AQAJ
        // TODO: Remove after AGP 3.4 release when this behavior will no longer be required.
        Implementation(AutoDisposeDetector::class.java,
            EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
            EnumSet.of(Scope.JAVA_FILE),
            EnumSet.of(Scope.TEST_SOURCES))
    )

    private const val OBSERVABLE = "io.reactivex.Observable"
    private const val FLOWABLE = "io.reactivex.Flowable"
    private const val PARALLEL_FLOWABLE = "io.reactivex.parallel.ParallelFlowable"
    private const val SINGLE = "io.reactivex.Single"
    private const val MAYBE = "io.reactivex.Maybe"
    private const val COMPLETABLE = "io.reactivex.Completable"

    // The default scopes for Android.
    private val DEFAULT_SCOPES = listOf("androidx.lifecycle.LifecycleOwner",
        "com.uber.autodispose.ScopeProvider",
        "android.app.Activity",
        "android.app.Fragment")

    private val REACTIVE_TYPES = setOf(OBSERVABLE, FLOWABLE, PARALLEL_FLOWABLE, SINGLE, MAYBE,
        COMPLETABLE)

    internal const val PROPERTY_FILE = "gradle.properties"
  }

  // The scopes that are applicable for the lint check.
  // This includes the DEFAULT_SCOPES as well as any custom scopes
  // defined by the consumer.
  private lateinit var appliedScopes: Set<String>

  override fun beforeCheckRootProject(context: Context) {
    // Add the default Android scopes.
    val scopes = HashSet(DEFAULT_SCOPES)

    // Add the custom scopes defined in configuration.
    val props = Properties()
    context.project.propertyFiles.find { it.name == PROPERTY_FILE }?.apply {
      val content = StringReader(context.client.readFile(this).toString())
      props.load(content)
      props.getProperty(CUSTOM_SCOPE_KEY)?.let { scopeProperty ->
        val customScopes = scopeProperty.split(",")
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .toList()
        scopes.addAll(customScopes)
      }
    }
    appliedScopes = scopes
  }

  override fun getApplicableMethodNames(): List<String> = listOf("subscribe", "subscribeWith")

  @Suppress("OverridingDeprecatedMember") // We support AGP 3.2. Switch when 3.3 stable
  override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    val evaluator = context.evaluator

    if (isReactiveType(evaluator, method)
        && isInScope(evaluator, node.getContainingUClass())
        && isExpressionValueUnused(node)
    ) {
      context.report(ISSUE, node, context.getLocation(node), LINT_DESCRIPTION)
    }
  }

  /**
   * Checks if the calling method is in "scope" that can be handled by AutoDispose.
   *
   * If your `subscribe`/`subscribeWith` method is called in a scope
   * that is recognized by AutoDispose, this returns true. This indicates that
   * you're subscribing in a scope and therefore, you must handle the subscription.
   * Default scopes include Android activities, fragments and custom classes that
   * implement ScopeProvider.
   *
   * @param evaluator the java evaluator.
   * @param psiClass the calling class.
   * @return whether the `subscribe` method is called "in-scope".
   * @see appliedScopes
   */
  private fun isInScope(evaluator: JavaEvaluator, psiClass: PsiClass?): Boolean {
    psiClass?.let { callingClass ->
      return appliedScopes.any {
        evaluator.inheritsFrom(callingClass, it, false)
      }
    }
    return false
  }

  private fun isReactiveType(evaluator: JavaEvaluator, method: PsiMethod): Boolean {
    return REACTIVE_TYPES.any { evaluator.isMemberInClass(method, it) }
  }

  /**
   * Checks whether the given expression's return value is unused.
   *
   * Borrowed from https://android.googlesource.com/platform/tools/base/+/studio-master-dev/lint/libs/lint-checks/src/main/java/com/android/tools/lint/checks/CheckResultDetector.kt
   *
   * @param element the element to be analyzed.
   * @return whether the expression is unused.
   */
  private fun isExpressionValueUnused(element: UElement): Boolean {
    var prev = element.getParentOfType<UExpression>(
        UExpression::class.java, false
    ) ?: return true
    var curr = prev.uastParent ?: return true
    while (curr is UQualifiedReferenceExpression && curr.selector === prev) {
      prev = curr
      curr = curr.uastParent ?: return true
    }
    @Suppress("RedundantIf")
    if (curr is UBlockExpression) {
      if (curr.uastParent is ULambdaExpression) {
        // Lambda block: for now assume used (e.g. parameter
        // in call. Later consider recursing here to
        // detect if the lambda itself is unused.
        return false
      }
      // In Java, it's apparent when an expression is unused:
      // the parent is a block expression. However, in Kotlin it's
      // much trickier: values can flow through blocks and up through
      // if statements, try statements.
      //
      // In Kotlin, we consider an expression unused if its parent
      // is not a block, OR, the expression is not the last statement
      // in the block, OR, recursively the parent expression is not
      // used (e.g. you're in an if, but that if statement is itself
      // not doing anything with the value.)
      val block = curr
      val expression = prev
      val index = block.expressions.indexOf(expression)
      if (index == -1) {
        return true
      }
      if (index < block.expressions.size - 1) {
        // Not last child
        return true
      }
      // It's the last child: see if the parent is unused
      val parent = curr.uastParent ?: return true
      if (parent is UMethod || parent is UClassInitializer) {
        return true
      }
      return isExpressionValueUnused(parent)
    } else if (curr is UMethod && curr.isConstructor) {
      return true
    } else {
      // Some other non block node type, such as assignment,
      // method declaration etc: not unused
      // TODO: Make sure that a void/unit method inline declaration
      // works correctly
      return false
    }
  }
}
