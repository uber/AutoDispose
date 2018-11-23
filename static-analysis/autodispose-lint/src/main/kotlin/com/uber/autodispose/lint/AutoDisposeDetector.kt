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
import org.jetbrains.uast.getContainingClass
import org.jetbrains.uast.getContainingUClass
import java.lang.IllegalStateException
import java.util.EnumSet

/**
 * Detector which checks if your stream subscriptions are handled by AutoDispose.
 */
class AutoDisposeDetector: Detector(), SourceCodeScanner {

  companion object {
    val ISSUE: Issue = Issue.create(
        "AutoDisposeUsage",
        "Always apply an AutoDispose scope before subscribing within defined scoped elements.",
        "You're subscribing to an observable but not handling it's subscription. This "
            + "can result in memory leaks. You can avoid memory leaks by appending " +
            "`.as(autoDisposable(this))` before you subscribe.",
        Category.CORRECTNESS,
        10,
        Severity.ERROR,
        Implementation(AutoDisposeDetector::class.java, EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES)))

    private const val OBSERVABLE = "io.reactivex.Observable"
    private const val FLOWABLE = "io.reactivex.Flowable"
    private const val PARALLEL_FLOWABLE = "io.reactivex.parallel.ParallelFlowable"
    private const val SINGLE = "io.reactivex.Single"
    private const val MAYBE = "io.reactivex.Maybe"
    private const val COMPLETABLE = "io.reactivex.Completable"
  }

  override fun getApplicableMethodNames(): List<String> = listOf("subscribe")

  override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    val evaluator = context.evaluator

    if (isReactiveType(evaluator, method) && isInScope(evaluator, node.getContainingUClass())) {
      context.report(ISSUE, node, context.getLocation(node), "Always apply an AutoDispose " +
          "scope before subscribing within defined scoped elements.")
    }
  }

  private fun isInScope(evaluator: JavaEvaluator, psiClass: PsiClass?): Boolean {
    psiClass?.let {
      return evaluator.inheritsFrom(psiClass, "androidx.lifecycle.LifecycleOwner", false) ||
          evaluator.inheritsFrom(psiClass, "com.uber.autodispose.ScopeProvider", false) ||
          evaluator.inheritsFrom(psiClass, "com.uber.autodispose.lifecycle.LifecycleScopeProvider", false)
    }
    return false
  }

  private fun isReactiveType(evaluator: JavaEvaluator, method: PsiMethod): Boolean {
    return evaluator.isMemberInClass(method, OBSERVABLE) ||
        evaluator.isMemberInClass(method, FLOWABLE) ||
        evaluator.isMemberInClass(method, SINGLE) ||
        evaluator.isMemberInClass(method, MAYBE) ||
        evaluator.isMemberInClass(method, COMPLETABLE) ||
        evaluator.isMemberInClass(method, PARALLEL_FLOWABLE)
  }
}
