/*
 * Copyright (C) 2017. Uber Technologies
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

package com.uber.autodispose.errorprone;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import java.util.Set;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.matchers.Matchers.instanceMethod;

/**
 * Checker for subscriptions not binding to lifecycle in components with lifecycle.
 * Use -XepOpt:ClassesWithScope flag to add support for custom components with lifecycle.
 * The sample configuration for Conductor:
 * <pre><code>
 *   -XepOpt:ClassesWithScope=com.bluelinelabs.conductor.Controller,android.app.Activity
 * </code></pre>
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "UseAutoDispose",
    summary = "Always apply an AutoDispose scope before "
        + "subscribing within defined scoped elements.",
    tags = CONCURRENCY,
    severity = ERROR
)
public final class UseAutoDispose extends BugChecker
    implements MethodInvocationTreeMatcher {

  private static final String AS = "as";
  private static final String SUBSCRIBE = "subscribe";
  private static final ImmutableList<MethodMatchers.MethodNameMatcher> AS_CALL_MATCHERS =
      ImmutableList.<MethodMatchers.MethodNameMatcher>builder()
          .add(instanceMethod().onDescendantOf("io.reactivex.Single")
              .named(AS))
          .add(instanceMethod().onDescendantOf("io.reactivex.Observable")
              .named(AS))
          .add(instanceMethod().onDescendantOf("io.reactivex.Completable")
              .named(AS))
          .add(instanceMethod().onDescendantOf("io.reactivex.Flowable")
              .named(AS))
          .add(instanceMethod().onDescendantOf("io.reactivex.Maybe")
              .named(AS))
          .add(instanceMethod().onDescendantOf("io.reactivex.parallel.ParallelFlowable")
              .named(AS))
          .build();
  private static final ImmutableList<MethodMatchers.MethodNameMatcher> SUBSCRIBE_MATCHERS =
      ImmutableList.<MethodMatchers.MethodNameMatcher>builder()
          .add(instanceMethod().onDescendantOf("io.reactivex.Single")
              .named(SUBSCRIBE))
          .add(instanceMethod().onDescendantOf("io.reactivex.Observable")
              .named(SUBSCRIBE))
          .add(instanceMethod().onDescendantOf("io.reactivex.Completable")
              .named(SUBSCRIBE))
          .add(instanceMethod().onDescendantOf("io.reactivex.Flowable")
              .named(SUBSCRIBE))
          .add(instanceMethod().onDescendantOf("io.reactivex.Maybe")
              .named(SUBSCRIBE))
          .add(instanceMethod().onDescendantOf("io.reactivex.parallel.ParallelFlowable")
              .named(SUBSCRIBE))
          .build();
  private static final ImmutableSet<String> DEFAULT_CLASSES_WITH_LIFECYCLE =
      new ImmutableSet.Builder<String>().add("android.app.Activity")
          .add("android.app.Fragment")
          .add("com.uber.autodispose.LifecycleScopeProvider")
          .add("android.support.v4.app.Fragment")
          .add("androidx.fragment.app.Fragment")
          .add("android.arch.lifecycle.LifecycleOwner")
          .add("androidx.lifecycle.LifecycleOwner")
          .add("com.uber.autodispose.ScopeProvider")
          .build();

  /**
   * Matcher to find the as operator in the observable chain.
   */
  private static final Matcher<ExpressionTree> METHOD_NAME_MATCHERS =
      (Matcher<ExpressionTree>) (tree, state) -> {
        if (!(tree instanceof MethodInvocationTree)) {
          return false;
        }
        MethodInvocationTree invTree = (MethodInvocationTree) tree;

        ExpressionTree methodSelectTree = invTree.getMethodSelect();

        // MemberSelectTree is used only for member access expression.
        // This is not true for scenarios such as calling super constructor.
        if (!(methodSelectTree instanceof MemberSelectTree)) {
          return false;
        }

        final MemberSelectTree memberTree = (MemberSelectTree) methodSelectTree;
        if (!memberTree.getIdentifier()
            .contentEquals(AS)) {
          return false;
        }

        return AS_CALL_MATCHERS.stream()
            .filter(methodNameMatcher -> methodNameMatcher.matches(invTree, state))
            .map(methodNameMatcher -> {
              ExpressionTree arg = invTree.getArguments()
                  .get(0);
              final Type scoper =
                  state.getTypeFromString("com.uber.autodispose.AutoDisposeConverter");
              return ASTHelpers.isSubtype(ASTHelpers.getType(arg), scoper, state);
            })
            // Filtering the method invocation with name as
            // and has an argument of type AutoDisposeConverter.
            .filter(Boolean::booleanValue)
            .findFirst()
            .orElse(false);
      };

  private final Matcher<MethodInvocationTree> matcher;

  public UseAutoDispose() {
    this(ErrorProneFlags.empty());
  }

  public UseAutoDispose(ErrorProneFlags flags) {
    // Borrowed from getList() impl
    Optional<ImmutableSet<String>> inputClasses = flags.getList("ClassesWithScope")
        .map(ImmutableSet::copyOf);

    ImmutableSet<String> classesWithLifecycle = inputClasses.orElse(DEFAULT_CLASSES_WITH_LIFECYCLE);
    matcher = matcher(classesWithLifecycle);
  }

  private static Matcher<MethodInvocationTree> matcher(Set<String> classesWithLifecycle) {
    return (Matcher<MethodInvocationTree>) (tree, state) -> {

      ExpressionTree methodSelectTree = tree.getMethodSelect();

      // MemberSelectTree is used only for member access expression.
      // This is not true for scenarios such as calling super constructor.
      if (!(methodSelectTree instanceof MemberSelectTree)) {
        return false;
      }

      final MemberSelectTree memberTree = (MemberSelectTree) tree.getMethodSelect();
      if (!memberTree.getIdentifier()
          .contentEquals(SUBSCRIBE)) {
        return false;
      }

      boolean matchFound = SUBSCRIBE_MATCHERS.stream()
          .map(methodNameMatcher -> methodNameMatcher.matches(tree, state))
          .filter(Boolean::booleanValue) // Filtering the method invocation with name subscribe
          .findFirst()
          .orElse(false);

      if (!matchFound) {
        return false;
      }

      ClassTree enclosingClass = ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class);
      Type.ClassType enclosingClassType = ASTHelpers.getType(enclosingClass);

      return classesWithLifecycle.stream()
          .map(classWithLifecycle -> {
            Type lifecycleType = state.getTypeFromString(classWithLifecycle);
            return ASTHelpers.isSubtype(enclosingClassType, lifecycleType, state)
                && !METHOD_NAME_MATCHERS.matches(memberTree.getExpression(), state);
          })
          // Filtering the method invocation which is a
          // subtype of one of the classes with lifecycle and name as
          .filter(Boolean::booleanValue)
          .findFirst()
          .orElse(false);
    };
  }

  @Override public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (matcher.matches(tree, state)) {
      return buildDescription(tree).build();
    } else {
      return Description.NO_MATCH;
    }
  }

  @Override public String linkUrl() {
    return "https://github.com/uber/AutoDispose/wiki/Error-Prone-Checker";
  }
}
