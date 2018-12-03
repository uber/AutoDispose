/*
 * Copyright 2012 The Error Prone Authors.
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

package com.uber.autodispose.errorprone;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import javax.lang.model.type.TypeKind;

import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.enclosingNode;
import static com.google.errorprone.matchers.Matchers.expressionStatement;
import static com.google.errorprone.matchers.Matchers.isLastStatementInBlock;
import static com.google.errorprone.matchers.Matchers.kindIs;
import static com.google.errorprone.matchers.Matchers.methodSelect;
import static com.google.errorprone.matchers.Matchers.nextStatement;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.Matchers.parentNode;
import static com.google.errorprone.matchers.Matchers.previousStatement;
import static com.google.errorprone.matchers.Matchers.toType;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static com.google.errorprone.util.ASTHelpers.getReturnType;
import static com.google.errorprone.util.ASTHelpers.getSymbol;
import static com.google.errorprone.util.ASTHelpers.getType;
import static com.google.errorprone.util.ASTHelpers.isVoidType;

/**
 * An abstract base class to match method invocations in which the return value is not used.
 * <p>
 * Adapted from https://github
 * .com/google/error-prone/blob/976ab7c62771d54dfe11bde9b01dc301d364957b/core/src/main/java/com
 * /google/errorprone/bugpatterns/AbstractReturnValueIgnored.java
 *
 * @author eaftan@google.com (Eddie Aftandilian)
 */
abstract class AbstractReturnValueIgnored extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return matchMethodInvocationNew(tree, state);
  }

  private Description matchMethodInvocationNew(MethodInvocationTree tree, VisitorState state) {
    boolean matches = specializedMatcher().matches(tree, state);
    if (!matches) {
      return Description.NO_MATCH;
    }

    if (lenient()) {
      boolean isUnusedReturnValue = allOf(
          parentNode(anyOf(
              AbstractReturnValueIgnored::isVoidReturningLambdaExpression,
              Matchers.kindIs(Kind.EXPRESSION_STATEMENT)
          )),
          not(methodSelect(toType(IdentifierTree.class, identifierHasName("super")))),
          not((t, s) -> isVoidType(getType(t), s)),
          not(AbstractReturnValueIgnored::expectedExceptionTest)
      ).matches(tree, state);
      if (!isUnusedReturnValue) {
        if (isValidReturnValueType(tree, state)) {
          return Description.NO_MATCH;
        }
      }
    }
    return describe(tree, state);
  }

  @Override public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    boolean matches = specializedMatcher().matches(tree, state);
    if (!matches) {
      return Description.NO_MATCH;
    }

    if (lenient()) {
      boolean isUnusedReturnValue = allOf(
          AbstractReturnValueIgnored::isNotConstructorReference,
          AbstractReturnValueIgnored::isVoidReturningMethodReferenceExpression,
          // Skip cases where the method we're referencing really does return void. We're only
          // looking for cases where the referenced method does not return void, but it's being
          // used on a void-returning functional interface.
          not((t, s) -> isVoidType(getSymbol(tree).getReturnType(), s)),
          not((t, s) -> isThrowingFunctionalInterface(s, ((JCMemberReference) t).type))
      ).matches(tree, state);
      if (!isUnusedReturnValue) {
        if (isValidReturnValueType(tree, state)) {
          return Description.NO_MATCH;
        }
      }
    }
    return describeMatch(tree);
  }

  private static boolean isNotConstructorReference(MemberReferenceTree tree, VisitorState state) {
    return tree.getMode() == ReferenceMode.INVOKE;
  }

  private static boolean isVoidReturningMethodReferenceExpression(MemberReferenceTree tree,
      VisitorState state) {
    return functionalInterfaceReturnsExactlyVoid(((JCMemberReference) tree).type, state);
  }

  private static boolean isVoidReturningLambdaExpression(Tree tree, VisitorState state) {
    if (!(tree instanceof LambdaExpressionTree)) {
      return false;
    }

    return functionalInterfaceReturnsExactlyVoid(((JCLambda) tree).type, state);
  }

  /**
   * Checks that the return value of a functional interface is void. Note, we do not use
   * ASTHelpers.isVoidType here, return values of Void are actually type-checked. Only
   * void-returning functions silently ignore return values of any type.
   */
  private static boolean functionalInterfaceReturnsExactlyVoid(Type interfaceType,
      VisitorState state) {
    return state.getTypes().findDescriptorType(interfaceType).getReturnType().getKind()
        == TypeKind.VOID;
  }

  private static boolean methodCallInDeclarationOfThrowingRunnable(VisitorState state) {
    // Find the nearest definitional context for this method invocation
    // (i.e.: the nearest surrounding class or lambda)
    Tree tree = null;
    out:
    for (Tree t : state.getPath()) {
      switch (t.getKind()) {
        case LAMBDA_EXPRESSION:
        case CLASS:
          tree = t;
          break out;
        default: // fall out, to loop again
      }
    }

    if (tree == null) {
      // Huh. Shouldn't happen.
      return false;
    }
    return isThrowingFunctionalInterface(state, getType(tree));
  }

  private static boolean isThrowingFunctionalInterface(VisitorState state, Type clazzType) {
    return CLASSES_CONSIDERED_THROWING.stream()
        .anyMatch(t -> ASTHelpers.isSubtype(clazzType, state.getTypeFromString(t), state));
  }

  /**
   * {@code @FunctionalInterface}'s that are generally used as a lambda expression for 'a block of
   * code that's going to fail', e.g.:
   *
   * <p>{@code assertThrows(FooException.class, () -> myCodeThatThrowsAnException());
   * errorCollector.checkThrows(FooException.class, () -> myCodeThatThrowsAnException()); }
   *
   * <p>// TODO(glorioso): Consider a meta-annotation like @LikelyToThrow instead/in addition?
   */
  private static final ImmutableSet<String> CLASSES_CONSIDERED_THROWING = ImmutableSet.of(
      "org.junit.function.ThrowingRunnable",
      "org.junit.jupiter.api.function.Executable",
      "org.assertj.core.api.ThrowableAssert$ThrowingCallable",
      "com.google.truth.ExpectFailure.AssertionCallback",
      "com.google.truth.ExpectFailure.DelegatedAssertionCallback",
      "com.google.truth.ExpectFailure.StandardSubjectBuilderCallback",
      "com.google.truth.ExpectFailure.SimpleSubjectBuilderCallback"
  );

  /**
   * Match whatever additional conditions concrete subclasses want to match (a list of known
   * side-effect-free methods, has a @OptionalCheckReturnValue annotation, etc.).
   */
  public abstract Matcher<? super ExpressionTree> specializedMatcher();

  /**
   * @return {@code true} if this should be lenient and only run the checks if the return value is
   *     ignored, {@code false} if it should always check {@link #specializedMatcher()}.
   */
  abstract boolean lenient();

  /**
   * Extracts the {@link Type} of the return value for {@link MethodInvocationTree} or {@link
   * MemberReferenceTree}, then checks it against {@link #capturedTypeAllowed(Type, VisitorState)}.
   */
  private boolean isValidReturnValueType(ExpressionTree tree, VisitorState state) {
    Type returnType = null;
    if (tree instanceof MethodInvocationTree) {
      returnType = getReturnType(((JCMethodInvocation) tree).getMethodSelect());
    } else if (tree instanceof MemberReferenceTree) {
      // Get the return type of the target referenced interface
      returnType =
          state.getTypes().findDescriptorType(((JCMemberReference) tree).type).getReturnType();
    }
    if (returnType != null) {
      return capturedTypeAllowed(returnType, state);
    }
    return true;
  }

  /**
   * Called only when lenient and return type is captured.
   *
   * @param type the {@link Type}
   * @param state the {@link VisitorState}
   * @return {@code true} if the captured return type is allowed in lenient cases, {@code false} if
   *     not.
   */
  protected boolean capturedTypeAllowed(Type type, VisitorState state) {
    return true;
  }

  private static Matcher<IdentifierTree> identifierHasName(final String name) {
    return (item, state) -> item.getName().contentEquals(name);
  }

  /**
   * Fixes the error by assigning the result of the call to the receiver reference, or deleting the
   * method call.
   */
  private Description describe(MethodInvocationTree methodInvocationTree, VisitorState state) {
    // Find the root of the field access chain, i.e. a.intern().trim() ==> a.
    ExpressionTree identifierExpr = ASTHelpers.getRootAssignable(methodInvocationTree);
    String identifierStr = null;
    Type identifierType = null;
    if (identifierExpr != null) {
      identifierStr = identifierExpr.toString();
      if (identifierExpr instanceof JCIdent) {
        identifierType = ((JCIdent) identifierExpr).sym.type;
      } else if (identifierExpr instanceof JCFieldAccess) {
        identifierType = ((JCFieldAccess) identifierExpr).sym.type;
      } else {
        throw new IllegalStateException("Expected a JCIdent or a JCFieldAccess");
      }
    }

    Type returnType = getReturnType(((JCMethodInvocation) methodInvocationTree).getMethodSelect());

    Fix fix;
    if (identifierStr != null
        && !"this".equals(identifierStr)
        && returnType != null
        && state.getTypes().isAssignable(returnType, identifierType)) {
      // Fix by assigning the assigning the result of the call to the root receiver reference.
      fix = SuggestedFix.prefixWith(methodInvocationTree, identifierStr + " = ");
    } else {
      // Unclear what the programmer intended.  Delete since we don't know what else to do.
      Tree parent = state.getPath().getParentPath().getLeaf();
      fix = SuggestedFix.delete(parent);
    }
    return describeMatch(methodInvocationTree, fix);
  }

  /** Allow return values to be ignored in tests that expect an exception to be thrown. */
  private static boolean expectedExceptionTest(Tree tree, VisitorState state) {
    if (mockitoInvocation(tree, state)) {
      return true;
    }

    // Allow unused return values in tests that check for thrown exceptions, e.g.:
    //
    // try {
    //   Foo.newFoo(-1);
    //   fail();
    // } catch (IllegalArgumentException expected) {
    // }
    //
    StatementTree statement = ASTHelpers.findEnclosingNode(state.getPath(), StatementTree.class);
    if (statement != null && EXPECTED_EXCEPTION_MATCHER.matches(statement, state)) {
      return true;
    }
    return false;
  }

  private static final Matcher<ExpressionTree> FAIL_METHOD = anyOf(
      instanceMethod().onDescendantOf("com.google.common.truth.AbstractVerb").named("fail"),
      instanceMethod().onDescendantOf("com.google.common.truth.StandardSubjectBuilder")
          .named("fail"),
      staticMethod().onClass("org.junit.Assert").named("fail"),
      staticMethod().onClass("junit.framework.Assert").named("fail"),
      staticMethod().onClass("junit.framework.TestCase").named("fail")
  );

  private static final Matcher<StatementTree> EXPECTED_EXCEPTION_MATCHER = anyOf(
      // expectedException.expect(Foo.class); me();
      allOf(
          isLastStatementInBlock(),
          previousStatement(expressionStatement(anyOf(instanceMethod().onExactClass(
              "org.junit.rules.ExpectedException"))))
      ),
      // try { me(); fail(); } catch (Throwable t) {}
      allOf(enclosingNode(kindIs(Kind.TRY)), nextStatement(expressionStatement(FAIL_METHOD))),
      // assertThrows(Throwable.class, () => { me(); })
      allOf(
          anyOf(isLastStatementInBlock(), parentNode(kindIs(Kind.LAMBDA_EXPRESSION))),
          // Within the context of a ThrowingRunnable/Executable:
          (t, s) -> methodCallInDeclarationOfThrowingRunnable(s)
      )
  );

  private static final Matcher<ExpressionTree> MOCKITO_MATCHER = anyOf(
      staticMethod().onClass("org.mockito.Mockito").named("verify"),
      instanceMethod().onDescendantOf("org.mockito.stubbing.Stubber").named("when"),
      instanceMethod().onDescendantOf("org.mockito.InOrder").named("verify")
  );

  /**
   * Don't match the method that is invoked through {@code Mockito.verify(t)} or {@code
   * doReturn(val).when(t)}.
   */
  private static boolean mockitoInvocation(Tree tree, VisitorState state) {
    if (!(tree instanceof JCMethodInvocation)) {
      return false;
    }
    JCMethodInvocation invocation = (JCMethodInvocation) tree;
    if (!(invocation.getMethodSelect() instanceof JCFieldAccess)) {
      return false;
    }
    ExpressionTree receiver = ASTHelpers.getReceiver(invocation);
    return MOCKITO_MATCHER.matches(receiver, state);
  }
}
