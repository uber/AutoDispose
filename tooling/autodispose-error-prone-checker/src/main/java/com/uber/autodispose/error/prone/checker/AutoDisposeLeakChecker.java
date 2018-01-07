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

package com.uber.autodispose.error.prone.checker;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.text.html.Option;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.instanceMethod;

/**
 * Checker for subscriptions not binding to lifecycle in components with lifecycle.
 * Use -XepOpt:AutoDisposeLeakCheck to add support for custom components with lifecycle.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "AutoDisposeLeakChecker",
    summary = "Always apply an Autodispose scope before subscribing",
    tags = {BugPattern.StandardTags.CONCURRENCY},
    severity = ERROR
)
public final class AutoDisposeLeakChecker extends BugChecker
    implements MethodInvocationTreeMatcher {

  private static final String AS = "as";
  private static final ImmutableList<MethodMatchers.MethodNameMatcher> AS_CALL_MATCHERS;
  private static final ImmutableList<MethodMatchers.MethodNameMatcher> SUBSCRIBE_MATCHERS;
  private static final String SUBSCRIBE = "subscribe";

  private final Matcher<MethodInvocationTree> matcher;

  public AutoDisposeLeakChecker() {
    this(ErrorProneFlags.empty());
  }

  public AutoDisposeLeakChecker(ErrorProneFlags flags) {
    Optional<ImmutableList<String>> inputClasses = flags.getList("AutoDisposeLeakCheck");
    ImmutableList<String> defaultClassesWithLifecycle = new ImmutableList.Builder<String>()
        .add("android.app.Activity")
        .add("android.app.Fragment")
        .add("com.uber.autodispose.LifecycleScopeProvider")
        .add("android.support.v4.app.Fragment")
        .add("android.arch.lifecycle.LifecycleOwner")
        .add("com.uber.autodispose.ScopeProvider")
        .build();
    ImmutableList<String> classesWithLifecycle = inputClasses.orElse(defaultClassesWithLifecycle);
    matcher = matcher(classesWithLifecycle);
  }

  static {
    AS_CALL_MATCHERS = new ImmutableList.Builder<MethodMatchers.MethodNameMatcher>()
        .add(instanceMethod().onDescendantOf("io.reactivex.Single").named(AS))
        .add(instanceMethod().onDescendantOf("io.reactivex.Observable").named(AS))
        .add(instanceMethod().onDescendantOf("io.reactivex.Completable").named(AS))
        .add(instanceMethod().onDescendantOf("io.reactivex.Flowable").named(AS))
        .add(instanceMethod().onDescendantOf("io.reactivex.Maybe").named(AS))
        .build();

    SUBSCRIBE_MATCHERS = new ImmutableList.Builder<MethodMatchers.MethodNameMatcher>()
        .add(instanceMethod().onDescendantOf("io.reactivex.Single").named(SUBSCRIBE))
        .add(instanceMethod().onDescendantOf("io.reactivex.Observable").named(SUBSCRIBE))
        .add(instanceMethod().onDescendantOf("io.reactivex.Completable").named(SUBSCRIBE))
        .add(instanceMethod().onDescendantOf("io.reactivex.Flowable").named(SUBSCRIBE))
        .add(instanceMethod().onDescendantOf("io.reactivex.Maybe").named(SUBSCRIBE))
        .build();
  }

  /**
   * Matcher to find the as operator in the observable chain.
   */
  private static final Matcher<ExpressionTree> METHOD_NAME_MATCHERS =
      new Matcher<ExpressionTree>() {
        @Override
        public boolean matches(ExpressionTree tree, VisitorState state) {
          if (!(tree instanceof MethodInvocationTree)) {
            return false;
          }
          MethodInvocationTree invTree = (MethodInvocationTree) tree;

          final MemberSelectTree memberTree = (MemberSelectTree) invTree.getMethodSelect();
          if (!memberTree.getIdentifier().contentEquals(AS)) {
            return false;
          }

          return AS_CALL_MATCHERS
              .stream()
              .filter(methodNameMatcher -> methodNameMatcher.matches(invTree, state))
              .map(methodNameMatcher -> {
                ExpressionTree arg = invTree.getArguments().get(0);
                final Type scoper = state
                    .getTypeFromString("com.uber.autodispose.AutoDisposeConverter");
                return ASTHelpers.isSubtype(ASTHelpers.getType(arg), scoper, state);
              })
              .filter(aBoolean -> aBoolean)
              .findFirst()
              .orElse(false);
        }
      };

  private static Matcher<MethodInvocationTree> matcher(List<String> classesWithLifecycle) {
    return (Matcher<MethodInvocationTree>) (tree, state) -> {

      boolean matchFound = false;
      final MemberSelectTree memberTree = (MemberSelectTree) tree.getMethodSelect();
      if (!memberTree.getIdentifier().contentEquals(SUBSCRIBE)) {
        return false;
      }

      for (MethodMatchers.MethodNameMatcher nameMatcher : SUBSCRIBE_MATCHERS) {
        if (nameMatcher.matches(tree, state)) {
          matchFound = true;
          break;
        }
      }
      if (!matchFound) {
        return false;
      }

      ClassTree enclosingClass =
          ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class);
      Type.ClassType enclosingClassType = ASTHelpers.getType(enclosingClass);

      return classesWithLifecycle
          .stream()
          .map(s -> {
            Type lifecycleType = state.getTypeFromString(s);
            return ASTHelpers.isSubtype(enclosingClassType, lifecycleType, state)
                && !METHOD_NAME_MATCHERS.matches(memberTree.getExpression(), state);
          })
          .filter(aBoolean -> aBoolean)
          .findFirst()
          .orElse(false);
    };
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (matcher.matches(tree, state)) {
      return buildDescription(tree).build();
    } else {
      return Description.NO_MATCH;
    }
  }

  @Override
  public String linkUrl() {
    return "https://github.com/uber/AutoDispose/wiki/Error-Prone-Checker";
  }
}
