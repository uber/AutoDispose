/*
 * Copyright (C) 2019. Uber Technologies
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
package autodispose2.lint

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiUtil
import java.io.StringReader
import java.util.EnumSet
import java.util.Properties
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UCallableReferenceExpression
import org.jetbrains.uast.UClassInitializer
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.visitor.AbstractUastVisitor

internal const val CUSTOM_SCOPE_KEY = "autodispose.typesWithScope"
internal const val LENIENT = "autodispose.lenient"
internal const val OVERRIDE_SCOPES = "autodispose.overrideScopes"
internal const val KOTLIN_EXTENSION_FUNCTIONS = "autodispose.kotlinExtensionFunctions"

/**
 * Detector which checks if your stream subscriptions are handled by AutoDispose.
 */
class AutoDisposeDetector : Detector(), SourceCodeScanner {

  companion object {
    internal const val LINT_DESCRIPTION = "Missing Disposable handling: Apply AutoDispose or cache " +
        "the Disposable instance manually and enable lenient mode."

    val ISSUE: Issue = Issue.create(
        "AutoDispose",
        LINT_DESCRIPTION,
        "You're subscribing to an observable but not handling its subscription. This " +
            "can result in memory leaks. You can avoid memory leaks by appending " +
            "`.as(autoDisposable(this))` before you subscribe or cache the Disposable instance" +
            " manually and enable lenient mode. More: https://github.com/uber/AutoDispose/wiki/Lint-Check",
        Category.CORRECTNESS,
        10,
        Severity.ERROR,
        // We use the overloaded constructor that takes a varargs of `Scope` as the last param.
        // This is to enable on-the-fly IDE checks. We are telling lint to run on both
        // JAVA and TEST_SOURCES in the `scope` parameter but by providing the `analysisScopes`
        // params, we're indicating that this check can run on either JAVA or TEST_SOURCES and
        // doesn't require both of them together.
        // From discussion on lint-dev https://groups.google.com/d/msg/lint-dev/ULQMzW1ZlP0/1dG4Vj3-AQAJ
        // TODO: This was supposed to be fixed in AS 3.4 but still required as recently as 3.6-alpha10.
        Implementation(AutoDisposeDetector::class.java,
            EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
            EnumSet.of(Scope.JAVA_FILE),
            EnumSet.of(Scope.TEST_SOURCES))
    )

    private const val OBSERVABLE = "io.reactivex.rxjava3.core.Observable"
    private const val FLOWABLE = "io.reactivex.rxjava3.core.Flowable"
    private const val PARALLEL_FLOWABLE = "io.reactivex.rxjava3.core.parallel.ParallelFlowable"
    private const val SINGLE = "io.reactivex.rxjava3.core.Single"
    private const val MAYBE = "io.reactivex.rxjava3.core.Maybe"
    private const val COMPLETABLE = "io.reactivex.rxjava3.core.Completable"
    private const val KOTLIN_EXTENSIONS = "autodispose2.KotlinExtensions"

    // The default scopes for Android.
    private val DEFAULT_SCOPES = listOf("androidx.lifecycle.LifecycleOwner",
        "autodispose2.ScopeProvider",
        "android.app.Activity",
        "android.app.Fragment")

    private val REACTIVE_TYPES = setOf(OBSERVABLE, FLOWABLE, PARALLEL_FLOWABLE, SINGLE, MAYBE,
        COMPLETABLE)

    private val REACTIVE_SUBSCRIBE_METHOD_NAMES = setOf("subscribe", "subscribeWith")

    internal const val PROPERTY_FILE = "gradle.properties"
  }

  // The scopes that are applicable for the lint check.
  // This includes the DEFAULT_SCOPES as well as any custom scopes
  // defined by the consumer.
  private lateinit var appliedScopes: Set<String>
  private lateinit var ktExtensionMethodToPackageMap: Multimap<String, String>
  private lateinit var appliedMethodNames: List<String>

  private var lenient: Boolean = false

  override fun beforeCheckRootProject(context: Context) {
    var overrideScopes = false
    val scopes = mutableSetOf<String>()
    val ktExtensionMethodToPackageMap = HashMultimap.create<String, String>()

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
      props.getProperty(KOTLIN_EXTENSION_FUNCTIONS)?.let { ktExtensionProperty ->
        ktExtensionProperty.split(",")
            .forEach {
              val arr = it.split("#", limit = 2)
              if (arr.size >= 2) {
                val (packageName, methodName) = arr
                ktExtensionMethodToPackageMap.put(methodName, packageName)
              }
            }
      }
      props.getProperty(LENIENT)?.toBoolean()?.let {
        lenient = it
      }
      props.getProperty(OVERRIDE_SCOPES)?.toBoolean()?.let {
        overrideScopes = it
      }
    }
    // If scopes are not overridden, add the default ones.
    if (!overrideScopes) {
      scopes.addAll(DEFAULT_SCOPES)
    }
    this.appliedScopes = scopes
    this.ktExtensionMethodToPackageMap = ktExtensionMethodToPackageMap
    this.appliedMethodNames = (REACTIVE_SUBSCRIBE_METHOD_NAMES + ktExtensionMethodToPackageMap.keySet()).toList()
  }

  override fun getApplicableMethodNames(): List<String> = appliedMethodNames

  override fun createUastHandler(context: JavaContext): UElementHandler? {
    return object : UElementHandler() {
      override fun visitCallableReferenceExpression(node: UCallableReferenceExpression) {
        node.resolve()?.let { method ->
          if (method is PsiMethod) {
            callableReferenceChecker(context, node, method, ::containingClassScopeChecker)
          }
        }
      }

      override fun visitCallExpression(node: UCallExpression) {
        node.resolve()?.let { method ->
          // Check if it's one of our withScope() higher order functions. If so, we handle that
          // separately and visit the passed in lambda body and run the subscribe method call checks
          // inside it with the "isInScope" check just hardcoded to true.
          if (method.name == "withScope" &&
              method.containingClass?.qualifiedName == KOTLIN_EXTENSIONS) {
            val args = node.valueArguments
            if (args.size == 2) {
              val last = args[1]
              // Check the lambda type too because it's a cheaper instance check
              if (last is ULambdaExpression) {
                // This is the AutoDisposeContext.() call
                // TODO we can't determine this exactly with lint as far as I can tell

                val body = last.body
                val visitor = SubscribeCallVisitor(
                    context,
                    callExpressionChecker = { context, node, calledMethod ->
                      callExpressionChecker(context, node, calledMethod) { _, _ -> true }
                    },
                    callableReferenceChecker = { context, node, calledMethod ->
                      callableReferenceChecker(context, node, calledMethod) { _, _ -> true }
                    }
                )
                body.accept(visitor)
                return@let
              }
            }
          }
          callExpressionChecker(context, node, method, ::containingClassScopeChecker)
        }
      }
    }
  }

  private fun callExpressionChecker(
    context: JavaContext,
    node: UCallExpression,
    method: PsiMethod,
    isInScope: (JavaEvaluator, UCallExpression) -> Boolean
  ) {
    evaluateMethodCall(node, method, context, isInScope)
  }

  private fun callableReferenceChecker(
    context: JavaContext,
    node: UCallableReferenceExpression,
    method: PsiMethod,
    isInScope: (JavaEvaluator, UCallExpression) -> Boolean
  ) {
    // Check if the resolved call reference is method and check that it's invocation is a
    // call expression so that we can get it's return type etc.
    if (node.uastParent != null && node.uastParent is UCallExpression) {
      evaluateMethodCall(node.uastParent as UCallExpression, method, context, isInScope)
    }
  }

  private class SubscribeCallVisitor(
    private val context: JavaContext,
    private val callExpressionChecker: (JavaContext, UCallExpression, PsiMethod) -> Unit,
    private val callableReferenceChecker: (JavaContext, UCallableReferenceExpression, PsiMethod) -> Unit
  ) : AbstractUastVisitor() {

    override fun visitCallExpression(node: UCallExpression): Boolean {
      node.resolve()?.let { callExpressionChecker(context, node, it) }
      return super.visitCallExpression(node)
    }

    override fun afterVisitCallableReferenceExpression(node: UCallableReferenceExpression) {
      node.resolve()?.let {
        if (it is PsiMethod) {
          callableReferenceChecker(context, node, it)
        }
      }
      super.afterVisitCallableReferenceExpression(node)
    }
  }

  override fun getApplicableUastTypes(): List<Class<out UElement>> =
      listOf(UCallExpression::class.java, UCallableReferenceExpression::class.java)

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
   * @param node the call expression.
   * @return whether the `subscribe` method is called "in-scope".
   * @see appliedScopes
   */
  private fun containingClassScopeChecker(evaluator: JavaEvaluator, node: UCallExpression): Boolean {
    node.getContainingUClass()?.let { callingClass ->
      return appliedScopes.any {
        evaluator.inheritsFrom(callingClass, it, false)
      }
    }
    return false
  }

  private fun isReactiveType(evaluator: JavaEvaluator, method: PsiMethod): Boolean {
    return REACTIVE_SUBSCRIBE_METHOD_NAMES.contains(method.name) && REACTIVE_TYPES.any {
      evaluator.isMemberInClass(method, it)
    }
  }

  private fun isKotlinExtension(evaluator: JavaEvaluator, method: PsiMethod): Boolean {
    return ktExtensionMethodToPackageMap.get(method.name).any {
      evaluator.isMemberInClass(method, it)
    }
  }

  /**
   * Returns whether the given [returnType] is allowed to bypass the lint check.
   *
   * If a `subscribe`/`subscribeWith` method return type is captured by the consumer
   * AND the return type implements Disposable, we let it bypass the lint check.
   * For example, subscribing with a plain Observer instead of a DiposableObserver will
   * not bypass the lint check since Observer doesn't extend Disposable.
   *
   * @param returnType the return type of the `subscribe`/`subscribeWith` call.
   * @param evaluator the evaluator.
   * @return whether the return type is allowed to bypass the lint check.
   */
  private fun isCapturedTypeAllowed(returnType: PsiType?, evaluator: JavaEvaluator): Boolean {
    PsiUtil.resolveClassInType(returnType)?.let {
      return evaluator.inheritsFrom(it, "io.reactivex.rxjava3.disposables.Disposable", false)
    }
    return false
  }

  /**
   * Evaluates the given [method] and it's expression.
   *
   * @param node the node which calls the method.
   * @param method the method representation.
   * @param context project context.
   */
  private fun evaluateMethodCall(
    node: UCallExpression,
    method: PsiMethod,
    context: JavaContext,
    isInScope: (JavaEvaluator, UCallExpression) -> Boolean
  ) {
    if (!getApplicableMethodNames().contains(method.name)) return
    val evaluator = context.evaluator

    if ((isReactiveType(evaluator, method) || isKotlinExtension(evaluator, method)) &&
        isInScope(evaluator, node)
    ) {
      if (!lenient) {
        context.report(ISSUE, node, context.getLocation(node), LINT_DESCRIPTION)
      } else {
        val isUnusedReturnValue = isExpressionValueUnused(node)
        if (isUnusedReturnValue || !isCapturedTypeAllowed(node.returnType, evaluator)) {
          // The subscribe return type isn't handled by consumer or the returned type
          // doesn't implement Disposable.
          context.report(ISSUE, node, context.getLocation(node), LINT_DESCRIPTION)
        }
      }
    }
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
