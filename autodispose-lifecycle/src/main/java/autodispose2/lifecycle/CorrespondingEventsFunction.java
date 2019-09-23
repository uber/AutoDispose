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
package autodispose2.lifecycle;

import autodispose2.OutsideScopeException;
import io.reactivex.rxjava3.functions.Function;

/**
 * A corresponding events function that acts as a normal {@link Function} but ensures a single event
 * type in the generic and tightens the possible exception thrown to {@link OutsideScopeException}.
 *
 * @param <E> the event type.
 */
public interface CorrespondingEventsFunction<E> extends Function<E, E> {

  /**
   * Given an event {@code event}, returns the next corresponding event that this lifecycle should
   * dispose on.
   *
   * @param event the source or start event.
   * @return the target event that should signal disposal.
   * @throws OutsideScopeException if the lifecycle exceeds its scope boundaries.
   */
  @Override
  E apply(E event) throws OutsideScopeException;
}
