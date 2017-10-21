package com.ubercab.autodispose.rxlifecycleinterop;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.trello.rxlifecycle2.RxLifecycle;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import javax.annotation.Nonnull;

final class LifeCycleProviderImpl implements LifecycleProvider<LifeCycleProviderImpl.Event> {

  private static final Function<Event, Event> CORRESPONDING_EVENTS = new Function<Event, Event>() {
    @Override public Event apply(Event event)
        throws Exception {
      switch (event) {
        case CREATE:
          return Event.DESTROY;
        default:
          throw new OutsideLifecycleException("Life cycle ended");
      }
    }
  };
  private final BehaviorSubject<Event> behaviorSubject = BehaviorSubject.create();

  @Nonnull @Override public Observable<Event> lifecycle() {
    return behaviorSubject.hide();
  }

  @Nonnull @Override
  public <T> LifecycleTransformer<T> bindUntilEvent(@Nonnull Event event) {
    return RxLifecycle.bindUntilEvent(behaviorSubject, event);
  }

  @Nonnull @Override public <T> LifecycleTransformer<T> bindToLifecycle() {
    return RxLifecycle.bind(behaviorSubject, CORRESPONDING_EVENTS);
  }

  void onCreate() {
    behaviorSubject.onNext(Event.CREATE);
  }

  void onDestroy() {
    behaviorSubject.onNext(Event.DESTROY);
  }

  enum Event {
    CREATE,
    DESTROY
  }
}
