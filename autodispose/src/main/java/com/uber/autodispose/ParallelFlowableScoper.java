package com.uber.autodispose;

import org.reactivestreams.Subscriber;

import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import io.reactivex.parallel.ParallelFlowable;

class ParallelFlowableScoper<T> extends Scoper
    implements Function<ParallelFlowable<? extends T>, ParallelFlowableSubscribeProxy<T>> {

  ParallelFlowableScoper(Maybe<?> lifecycle) {
    super(lifecycle);
  }

  @Override public ParallelFlowableSubscribeProxy<T> apply(
          final ParallelFlowable<? extends T> source) throws Exception {
    return new ParallelFlowableSubscribeProxy<T>() {
      @Override
      public void subscribe(Subscriber<? super T>[] subscribers) {
        new AutoDisposeParallelFlowable<>(source, scope()).subscribe(subscribers);
      }
    };
  }

  static final class AutoDisposeParallelFlowable<T> extends ParallelFlowable<T> {

    private final ParallelFlowable<T> source;
    private final Maybe<?> scope;

    AutoDisposeParallelFlowable(ParallelFlowable<T> source, Maybe<?> scope) {
      this.source = source;
      this.scope = scope;
    }

    @Override public void subscribe(Subscriber<? super T>[] subscribers) {
      Subscriber<? super T>[] newSubscribers = new Subscriber[subscribers.length];
      for (int i = 0; i < subscribers.length; i++) {
        AutoDisposingSubscriberImpl<? super T> subscriber =
            new AutoDisposingSubscriberImpl<>(scope, subscribers[i]);
        newSubscribers[i] = subscriber;
      }
      source.subscribe(newSubscribers);
    }

    @Override public int parallelism() {
      return source.parallelism();
    }
  }
}
