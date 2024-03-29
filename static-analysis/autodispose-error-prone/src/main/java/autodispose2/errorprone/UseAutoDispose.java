/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.errorprone;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.util.ASTHelpers.findEnclosingNode;
import static com.google.errorprone.util.ASTHelpers.getType;
import static com.google.errorprone.util.ASTHelpers.isSubtype;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.inject.Inject;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import java.util.Set;

/**
 * Checker for subscriptions not binding to lifecycle in components with lifecycle. Use
 * -XepOpt:TypesWithScope flag to add support for custom types with scope. Use
 * -XepOpt:OverrideScopes to only run the EP check on your custom types with scope. The sample
 * configuration for Conductor:
 *
 * <pre><code>
 *   -XepOpt:TypesWithScope=com.bluelinelabs.conductor.Controller,android.app.Activity
 *   -XepOpt:OverrideScopes=<true|false>
 * </code></pre>
 */
@SuppressWarnings("BugPatternNaming")
@AutoService(BugChecker.class)
@BugPattern(
    name = "AutoDispose",
    summary =
        "Missing Disposable handling: Apply AutoDispose or cache "
            + "the Disposable instance manually and enable lenient mode.",
    tags = CONCURRENCY,
    severity = ERROR)
public final class UseAutoDispose extends AbstractReturnValueIgnored
    implements MethodInvocationTreeMatcher {

  private static final String SUBSCRIBE = "subscribe";
  private static final String SUBSCRIBE_WITH = "subscribeWith";
  private static final ImmutableSet<String> DEFAULT_CLASSES_WITH_LIFECYCLE =
      new ImmutableSet.Builder<String>()
          .add("android.app.Activity")
          .add("android.app.Fragment")
          .add("androidx.fragment.app.Fragment")
          .add("androidx.lifecycle.LifecycleOwner")
          .add("autodispose2.ScopeProvider")
          .build();

  private static final Matcher<ExpressionTree> SUBSCRIBE_METHOD =
      anyOf(
          instanceMethod()
              .onDescendantOf("io.reactivex.rxjava3.core.Single")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod()
              .onDescendantOf("io.reactivex.rxjava3.core.Observable")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod()
              .onDescendantOf("io.reactivex.rxjava3.core.Completable")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod()
              .onDescendantOf("io.reactivex.rxjava3.core.Flowable")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod()
              .onDescendantOf("io.reactivex.rxjava3.core.Maybe")
              .namedAnyOf(SUBSCRIBE, SUBSCRIBE_WITH),
          instanceMethod()
              .onDescendantOf("io.reactivex.rxjava3.parallel.ParallelFlowable")
              .named(SUBSCRIBE));

  private final Supplier<Type> disposableTypeSupplier =
      VisitorState.memoize(
          state -> state.getTypeFromString("io.reactivex.rxjava3.disposables.Disposable"));
  private final Supplier<Type> subscriptionTypeSupplier =
      VisitorState.memoize(state -> state.getTypeFromString("org.reactivestreams.Subscription"));

  private final Matcher<ExpressionTree> matcher;
  private final boolean lenient;

  @SuppressWarnings("unused") // Default constructor used for SPI
  public UseAutoDispose() {
    this(ErrorProneFlags.empty());
  }

  @SuppressWarnings("WeakerAccess") // Public for ErrorProne
  @Inject
  public UseAutoDispose(ErrorProneFlags flags) {
    Optional<ImmutableSet<String>> inputClasses =
        flags.getList("TypesWithScope").map(ImmutableSet::copyOf);
    Optional<Boolean> overrideScopes = flags.getBoolean("OverrideScopes");

    ImmutableSet<String> classesWithScope = getClassesWithScope(inputClasses, overrideScopes);
    matcher = allOf(SUBSCRIBE_METHOD, matcher(classesWithScope));
    lenient = flags.getBoolean("Lenient").orElse(false);
  }

  @Override
  boolean lenient() {
    return lenient;
  }

  @Override
  public Matcher<? super ExpressionTree> specializedMatcher() {
    return matcher;
  }

  @Override
  protected boolean capturedTypeAllowed(Type type, VisitorState state) {
    return isSubtype(type, disposableTypeSupplier.get(state), state)
        || isSubtype(type, subscriptionTypeSupplier.get(state), state);
  }

  @Override
  public String linkUrl() {
    return "https://github.com/uber/AutoDispose/wiki/Error-Prone-Checker";
  }

  /**
   * Return the lifecycle classes on which to apply the Error-Prone check.
   *
   * @param inputClasses the custom scopes defined by user.
   * @param overrideScopes whether the custom scopes are exclusive.
   * @return the classes on which to apply the error-prone check.
   */
  private static ImmutableSet<String> getClassesWithScope(
      Optional<ImmutableSet<String>> inputClasses, Optional<Boolean> overrideScopes) {
    if (inputClasses.isPresent()) {
      if (overrideScopes.isPresent() && overrideScopes.get()) {
        // The custom scopes are exclusive, just return that.
        return inputClasses.get();
      } else {
        // The custom scopes aren't exclusive, so bundle them together with default scopes.
        return ImmutableSet.<String>builder()
            .addAll(DEFAULT_CLASSES_WITH_LIFECYCLE)
            .addAll(inputClasses.get())
            .build();
      }
    } else {
      // No custom scopes. Return default scopes.
      return DEFAULT_CLASSES_WITH_LIFECYCLE;
    }
  }

  private static Matcher<ExpressionTree> matcher(Set<String> classesWithLifecycle) {
    return (tree, state) -> {
      ClassTree enclosingClass = findEnclosingNode(state.getPath(), ClassTree.class);
      Type.ClassType enclosingClassType = getType(enclosingClass);

      return classesWithLifecycle.stream()
          .map(
              classWithLifecycle -> {
                Type lifecycleType = state.getTypeFromString(classWithLifecycle);
                return isSubtype(enclosingClassType, lifecycleType, state);
              })
          // Filtering the method invocation which is a
          // subtype of one of the classes with lifecycle and name as
          .filter(Boolean::booleanValue)
          .findFirst()
          .orElse(false);
    };
  }
}
