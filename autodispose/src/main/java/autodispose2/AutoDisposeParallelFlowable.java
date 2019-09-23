/*
 * Copyright (C) 2019. Uber Technologies
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
package autodispose2;

import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import org.reactivestreams.Subscriber;

final class AutoDisposeParallelFlowable<T> extends ParallelFlowable<T>
    implements ParallelFlowableSubscribeProxy<T> {

  private final ParallelFlowable<T> source;
  private final CompletableSource scope;

  AutoDisposeParallelFlowable(ParallelFlowable<T> source, CompletableSource scope) {
    this.source = source;
    this.scope = scope;
  }

  @Override
  public void subscribe(Subscriber<? super T>[] subscribers) {
    if (!validate(subscribers)) {
      return;
    }

    @SuppressWarnings("unchecked")
    Subscriber<? super T>[] newSubscribers = new Subscriber[subscribers.length];
    for (int i = 0; i < subscribers.length; i++) {
      AutoDisposingSubscriberImpl<? super T> subscriber =
          new AutoDisposingSubscriberImpl<>(scope, subscribers[i]);
      newSubscribers[i] = subscriber;
    }
    source.subscribe(newSubscribers);
  }

  @Override
  public int parallelism() {
    return source.parallelism();
  }
}
