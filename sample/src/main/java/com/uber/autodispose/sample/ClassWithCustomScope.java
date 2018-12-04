package com.uber.autodispose.sample;

import com.uber.autodispose.ScopeProvider;
import io.reactivex.Observable;

import static com.uber.autodispose.AutoDispose.autoDisposable;

public class ClassWithCustomScope implements CustomScope {

  void sampleCall() {
    // This class implements CustomScope, which we've informed the error prone and lint checks to
    // flag as a known type with a scope. If we comment out the autodispose line, this will fail
    // to compile or fail lint.
    Observable.just(1)
        .as(autoDisposable(ScopeProvider.UNBOUND))
        .subscribe();
  }

}
