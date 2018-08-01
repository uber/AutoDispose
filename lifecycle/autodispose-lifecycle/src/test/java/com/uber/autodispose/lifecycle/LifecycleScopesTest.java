/*
 * Copyright (C) 2018. Uber Technologies
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

package com.uber.autodispose.lifecycle;

import com.uber.autodispose.AutoDisposePlugins;
import com.uber.autodispose.OutsideScopeException;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import java.util.Comparator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.uber.autodispose.lifecycle.TestLifecycleScopeProvider.TestLifecycle.STOPPED;

public final class LifecycleScopesTest {

  @Before @After public void resetPlugins() {
    AutoDisposePlugins.reset();
  }

  @Test public void lifecycleDefault_shouldFailIfNotStarted() {
    TestLifecycleScopeProvider lifecycle = TestLifecycleScopeProvider.create();

    try {
      LifecycleScopes.resolveScopeFromLifecycle(lifecycle);
      throw new AssertionError(
          "Lifecycle resolution should have failed due to missing start event");
    } catch (LifecycleNotStartedException ignored) {

    }
  }

  @Test public void lifecycleDefault_withStart_shouldFailIfEnded() {
    TestLifecycleScopeProvider lifecycle = TestLifecycleScopeProvider.createInitial(STOPPED);

    try {
      LifecycleScopes.resolveScopeFromLifecycle(lifecycle);
      throw new AssertionError("Lifecycle resolution should have failed due to it being ended.");
    } catch (LifecycleEndedException ignored) {

    }
  }

  @Test public void lifecycleCheckEnd_shouldFailIfEndedWithNoHandler() {
    TestLifecycleScopeProvider lifecycle = TestLifecycleScopeProvider.createInitial(STOPPED);

    try {
      LifecycleScopes.resolveScopeFromLifecycle(lifecycle, true);
      throw new AssertionError("Lifecycle resolution should have failed due to it being ended.");
    } catch (LifecycleEndedException ignored) {

    }
  }

  @Test public void lifecycleCheckEnd_shouldFailIfEndedWithHandler() {
    TestLifecycleScopeProvider lifecycle = TestLifecycleScopeProvider.createInitial(STOPPED);

    AutoDisposePlugins.setOutsideScopeHandler(new Consumer<OutsideScopeException>() {
      @Override public void accept(OutsideScopeException e) {
        // Swallow the exception.
      }
    });

    LifecycleScopes.resolveScopeFromLifecycle(lifecycle, true)
        .test()
        .assertComplete();
  }

  @Test public void lifecycleCheckEnd_shouldFailIfEndedWithThrowingHandler() {
    TestLifecycleScopeProvider lifecycle = TestLifecycleScopeProvider.createInitial(STOPPED);

    final RuntimeException expected = new RuntimeException("Expected");
    AutoDisposePlugins.setOutsideScopeHandler(new Consumer<OutsideScopeException>() {
      @Override public void accept(OutsideScopeException e) {
        // Throw it back
        throw expected;
      }
    });

    LifecycleScopes.resolveScopeFromLifecycle(lifecycle, true)
        .test()
        .assertError(expected);
  }

  @Test public void lifecycleCheckEndDisabled_shouldRouteThroughOnError() {
    TestLifecycleScopeProvider lifecycle = TestLifecycleScopeProvider.createInitial(STOPPED);

    LifecycleScopes.resolveScopeFromLifecycle(lifecycle, false)
        .test()
        .assertError(LifecycleEndedException.class);
  }

  @Test public void resolveScopeFromLifecycle_normal() {
    PublishSubject<Integer> lifecycle = PublishSubject.create();

    TestObserver<?> o = LifecycleScopes.resolveScopeFromLifecycle(lifecycle, 3)
        .test();

    lifecycle.onNext(0);
    o.assertNotTerminated();
    lifecycle.onNext(1);
    o.assertNotTerminated();
    lifecycle.onNext(0);
    o.assertNotTerminated();
    lifecycle.onNext(2);
    o.assertNotTerminated();

    // Now we end
    lifecycle.onNext(3);
    o.assertComplete();
  }

  static class IntHolder {
    final int value;

    IntHolder(int value) {
      this.value = value;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      IntHolder intHolder = (IntHolder) o;

      return value == intHolder.value;
    }

    @Override public int hashCode() {
      return value;
    }
  }

  @Test public void resolveScopeFromLifecycle_normal_notComparable() {
    PublishSubject<IntHolder> lifecycle = PublishSubject.create();

    TestObserver<?> o = LifecycleScopes.resolveScopeFromLifecycle(lifecycle, new IntHolder(3))
        .test();

    lifecycle.onNext(new IntHolder(0));
    o.assertNotTerminated();
    lifecycle.onNext(new IntHolder(1));
    o.assertNotTerminated();
    lifecycle.onNext(new IntHolder(0));
    o.assertNotTerminated();
    lifecycle.onNext(new IntHolder(2));
    o.assertNotTerminated();

    // Now we end
    lifecycle.onNext(new IntHolder(3));
    o.assertComplete();
  }

  /**
   * An int wrapper that compares the negative {@link #value} to another integer.
   */
  static class NegativeComparableInteger implements Comparable<NegativeComparableInteger> {

    final int value;

    NegativeComparableInteger(int value) {
      this.value = value;
    }

    @Override public int compareTo(NegativeComparableInteger o) {
      return Integer.compare(-value, o.value);
    }
  }

  @Test public void resolveScopeFromLifecycle_normal_comparable() {
    PublishSubject<NegativeComparableInteger> lifecycle = PublishSubject.create();

    TestObserver<?> o =
        LifecycleScopes.resolveScopeFromLifecycle(lifecycle, new NegativeComparableInteger(3))
            .test();

    lifecycle.onNext(new NegativeComparableInteger(-1));
    o.assertNotTerminated();
    lifecycle.onNext(new NegativeComparableInteger(-2));
    o.assertNotTerminated();
    lifecycle.onNext(new NegativeComparableInteger(3));
    o.assertNotTerminated();

    // Now we end
    lifecycle.onNext(new NegativeComparableInteger(-3));
    o.assertComplete();
  }

  @Test public void resolveScopeFromLifecycle_normal_comparator() {
    PublishSubject<Integer> lifecycle = PublishSubject.create();

    Comparator<Integer> comparator = new Comparator<Integer>() {
      @Override public int compare(Integer o1, Integer o2) {
        return Integer.compare(-o1, o2);
      }
    };

    TestObserver<?> o = LifecycleScopes.resolveScopeFromLifecycle(lifecycle, 3, comparator)
        .test();

    lifecycle.onNext(-1);
    o.assertNotTerminated();
    lifecycle.onNext(-2);
    o.assertNotTerminated();
    lifecycle.onNext(3);
    o.assertNotTerminated();

    // Now we end
    lifecycle.onNext(-3);
    o.assertComplete();
  }

  @Test public void resolveScopeFromLifecycle_error() {
    PublishSubject<Integer> lifecycle = PublishSubject.create();

    TestObserver<?> o = LifecycleScopes.resolveScopeFromLifecycle(lifecycle, 3)
        .test();

    lifecycle.onNext(0);
    o.assertNotTerminated();
    lifecycle.onNext(1);
    o.assertNotTerminated();

    // Now we end
    RuntimeException expected = new RuntimeException("Expected");
    lifecycle.onError(expected);
    o.assertError(expected);
  }

  @Test public void resolveScopeFromLifecycle_complete() {
    PublishSubject<Integer> lifecycle = PublishSubject.create();

    TestObserver<?> o = LifecycleScopes.resolveScopeFromLifecycle(lifecycle, 3)
        .test();

    lifecycle.onNext(0);
    o.assertNotTerminated();
    lifecycle.onNext(1);
    o.assertNotTerminated();
    lifecycle.onNext(0);
    o.assertNotTerminated();
    lifecycle.onNext(2);
    o.assertNotTerminated();

    // Now we complete
    lifecycle.onComplete();
    o.assertComplete();
  }

  @Test public void resolveScopeFromLifecycle_complete_noFirstElement() {
    PublishSubject<Integer> lifecycle = PublishSubject.create();

    TestObserver<?> o = LifecycleScopes.resolveScopeFromLifecycle(lifecycle, 3)
        .test();

    // Now we complete
    lifecycle.onComplete();
    o.assertComplete();
  }

  @Test public void resolveScopeFromLifecycle_error_noFirstElement() {
    PublishSubject<Integer> lifecycle = PublishSubject.create();

    TestObserver<?> o = LifecycleScopes.resolveScopeFromLifecycle(lifecycle, 3)
        .test();

    // Now we error
    RuntimeException expected = new RuntimeException("Expected");
    lifecycle.onError(expected);
    o.assertError(expected);
  }
}
