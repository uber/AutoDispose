package com.uber.autodispose;

import org.reactivestreams.Subscriber;

import io.reactivex.Maybe;
import io.reactivex.parallel.ParallelFlowable;
import io.reactivex.parallel.ParallelFlowableConverter;

class ParallelFlowableScoper<T> extends Scoper
    implements ParallelFlowableConverter<T, ParallelFlowableSubscribeProxy<T>> {

  ParallelFlowableScoper(Maybe<?> scope) {
    super(scope);
  }

  @Override public ParallelFlowableSubscribeProxy<T> apply(final ParallelFlowable<T> upstream) {
    return new ParallelFlowableSubscribeProxy<T>() {
      @Override public void subscribe(Subscriber<? super T>[] subscribers) {
        new AutoDisposeParallelFlowable<>(upstream, scope()).subscribe(subscribers);
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
      if (!validate(subscribers)) {
        return;
      }

      // there's no efficient way to avoid this warning due to generics
      @SuppressWarnings("unchecked") Subscriber<? super T>[] newSubscribers = new Subscriber[subscribers.length];
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
