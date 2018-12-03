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
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
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
        "com.uber.autodispose.lifecycle.LifecycleScopeProvider",
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
            .map { it.trim() }
            .filter { it.isNotBlank() }
        scopes.addAll(customScopes)
      }
    }
    appliedScopes = scopes
  }

  override fun getApplicableMethodNames(): List<String> = listOf("subscribe", "subscribeWith")

  override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    val evaluator = context.evaluator

    if (isReactiveType(evaluator, method) && isInScope(evaluator, node.getContainingUClass())) {
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
}
