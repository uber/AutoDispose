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
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import java.util.Set;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
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
public final class UseAutoDispose extends AbstractReturnValueIgnored
    implements MethodInvocationTreeMatcher {

  private static final String AS = "as";
  private static final String SUBSCRIBE = "subscribe";
  private static final String SUBSCRIBE_WITH = "subscribeWith";
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

  private static final Matcher<ExpressionTree> SUBSCRIBE_METHOD =
      anyOf(instanceMethod().onDescendantOf("io.reactivex.Single")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod().onDescendantOf("io.reactivex.Observable")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod().onDescendantOf("io.reactivex.Completable")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod().onDescendantOf("io.reactivex.Flowable")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod().onDescendantOf("io.reactivex.Maybe")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod().onDescendantOf("io.reactivex.parallel.ParallelFlowable")
              .named(SUBSCRIBE));

  private final Matcher<ExpressionTree> matcher;

  public UseAutoDispose() {
    this(ErrorProneFlags.empty());
  }

  public UseAutoDispose(ErrorProneFlags flags) {
    Optional<ImmutableSet<String>> inputClasses = flags.getList("ClassesWithScope")
        .map(ImmutableSet::copyOf);

    ImmutableSet<String> classesWithLifecycle = inputClasses.orElse(DEFAULT_CLASSES_WITH_LIFECYCLE);
    matcher = allOf(SUBSCRIBE_METHOD, matcher(classesWithLifecycle));
  }

  @Override public Matcher<? super ExpressionTree> specializedMatcher() {
    return matcher;
  }

  @Override public String linkUrl() {
    return "https://github.com/uber/AutoDispose/wiki/Error-Prone-Checker";
  }

  private static Matcher<ExpressionTree> matcher(Set<String> classesWithLifecycle) {
    return (Matcher<ExpressionTree>) (tree, state) -> {
      ClassTree enclosingClass = ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class);
      Type.ClassType enclosingClassType = ASTHelpers.getType(enclosingClass);

      return classesWithLifecycle.stream()
          .map(classWithLifecycle -> {
            Type lifecycleType = state.getTypeFromString(classWithLifecycle);
            return ASTHelpers.isSubtype(enclosingClassType, lifecycleType, state);
          })
          // Filtering the method invocation which is a
          // subtype of one of the classes with lifecycle and name as
          .filter(Boolean::booleanValue)
          .findFirst()
          .orElse(false);
    };
  }
}
