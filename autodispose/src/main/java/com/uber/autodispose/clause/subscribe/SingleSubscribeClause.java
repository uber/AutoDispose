/*
 * Copyright (C) 2017. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uber.autodispose.clause.subscribe;

import com.uber.autodispose.observers.AutoDisposingSingleObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * Subscribe clause for the around steps that match Single's subscribe overloads.
 */
public interface SingleSubscribeClause {

  /**
   * Proxy for {@link Single#subscribe()}
   *
   * @param <T> the Single type
   * @return an {@link AutoDisposingSingleObserver}
   */
  <T> AutoDisposingSingleObserver<T> empty();

  /**
   * Proxy for {@link Single#subscribe(Consumer)}
   *
   * @param <T> the Single type
   * @return an {@link AutoDisposingSingleObserver}
   */
  <T> AutoDisposingSingleObserver<T> around(Consumer<? super T> onSuccess);

  /**
   * Proxy for {@link Single#subscribe(BiConsumer)}
   *
   * @param <T> the Single type
   * @return an {@link AutoDisposingSingleObserver}
   */
  <T> AutoDisposingSingleObserver<T> around(BiConsumer<? super T, ? super Throwable> biConsumer);

  /**
   * Proxy for {@link Single#subscribe(Consumer, Consumer)}
   *
   * @param <T> the Single type
   * @return an {@link AutoDisposingSingleObserver}
   */
  <T> AutoDisposingSingleObserver<T> around(Consumer<? super T> onSuccess,
      Consumer<? super Throwable> onError);

  /**
   * Proxy for {@link Single#subscribe(SingleObserver)}
   *
   * @param <T> the Single type
   * @return an {@link AutoDisposingSingleObserver}
   */
  <T> AutoDisposingSingleObserver<T> around(SingleObserver<T> observer);
}
