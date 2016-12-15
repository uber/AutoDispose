package com.uber.autodispose;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import javax.annotation.Nonnull;

final class TestUtil {
  private static final Function<Integer, Integer> CORRESPONDING_EVENTS = lastEvent -> {
    switch (lastEvent) {
      case 0:
        return 3;
      case 1:
        return 2;
      default:
        throw new LifecycleEndedException();
    }
  };

  private TestUtil() {
    throw new InstantiationError();
  }

  static LifecycleProvider<Integer> makeProvider(final BehaviorSubject<Integer> lifecycle) {
    return new LifecycleProvider<Integer>() {
      @Nonnull
      @Override
      public Observable<Integer> lifecycle() {
        return lifecycle;
      }

      @Nonnull
      @Override
      public Function<Integer, Integer> correspondingEvents() {
        return CORRESPONDING_EVENTS;
      }

      @Override
      public Integer peekLifecycle() {
        return lifecycle.getValue();
      }
    };
  }
}
