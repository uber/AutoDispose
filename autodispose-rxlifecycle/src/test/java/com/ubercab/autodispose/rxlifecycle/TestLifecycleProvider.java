package com.ubercab.autodispose.rxlifecycle;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.OutsideLifecycleException;
import com.trello.rxlifecycle2.RxLifecycle;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

final class TestLifecycleProvider implements LifecycleProvider<TestLifecycleProvider.Event> {

  private static final Function<Event, Event> CORRESPONDING_EVENTS = new Function<Event, Event>() {
    @Override public Event apply(Event event)
        throws Exception {
      switch (event) {
        case CREATE:
          return Event.DESTROY;
        default:
          throw new OutsideLifecycleException("Lifecycle ended");
      }
    }
  };

  private final BehaviorSubject<Event> lifecycle = BehaviorSubject.create();

  @Override public Observable<Event> lifecycle() {
    return lifecycle.hide();
  }

  @Override
  public <T> LifecycleTransformer<T> bindUntilEvent(Event event) {
    return RxLifecycle.bindUntilEvent(lifecycle, event);
  }

  @Override public <T> LifecycleTransformer<T> bindToLifecycle() {
    return RxLifecycle.bind(lifecycle, CORRESPONDING_EVENTS);
  }

  void emitCreate() {
    lifecycle.onNext(Event.CREATE);
  }

  void emitDestroy() {
    lifecycle.onNext(Event.DESTROY);
  }

  enum Event {
    CREATE,
    DESTROY
  }
}
