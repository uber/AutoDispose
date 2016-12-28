package com.uber.autodispose;

import hu.akarnokd.rxjava2.subjects.MaybeSubject;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import javax.annotation.Nonnull;

final class TestUtil {
  private static final Function<Integer, Integer> CORRESPONDING_EVENTS =
      new Function<Integer, Integer>() {
        @Override public Integer apply(Integer lastEvent) throws Exception {
          switch (lastEvent) {
            case 0:
              return 3;
            case 1:
              return 2;
            default:
              throw new LifecycleEndedException();
          }
        }
      };

  private TestUtil() {
    throw new InstantiationError();
  }

  static ScopeProvider makeProvider(final MaybeSubject<Integer> scope) {
    return new ScopeProvider() {
      @Override public Maybe<?> requestScope() {
        return scope;
      }
    };
  }

  static LifecycleScopeProvider<Integer> makeLifecycleProvider(
      final BehaviorSubject<Integer> lifecycle) {
    return new LifecycleScopeProvider<Integer>() {
      @Nonnull @Override public Observable<Integer> lifecycle() {
        return lifecycle;
      }

      @Nonnull @Override public Function<Integer, Integer> correspondingEvents() {
        return CORRESPONDING_EVENTS;
      }

      @Override public Integer peekLifecycle() {
        return lifecycle.getValue();
      }
    };
  }
}
