/*
 * Copyright (c) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 *
 * <pre><code>
 * public interface LifecycleScopeProvider<E> extends ScopeProvider {
 *   Observable<E> lifecycle();
 *
 *   Function<E, E> correspondingEvents();
 *
 *   E peekLifecycle();
 *
 *   // Inherited from ScopeProvider
 *   CompletableSource requestScope();
 * }
 * </pre></code>
 *
 * <p>A common use case for this is objects that have implicit lifecycles, such as Android's {@code
 * Activity}, {@code Fragment}, and {@code View} classes. Internally at subscription-time,
 * AutoDispose will resolve a {@link io.reactivex.CompletableSource} representation of the target
 * {@code end} event in the lifecycle, and exposes an API to dictate what corresponding events are
 * for the current lifecycle state (e.g. {@code ATTACH} -> {@code DETACH}). This also allows you to
 * enforce lifecycle boundary requirements, and by default will error if the lifecycle has either
 * not started yet or has already ended.
 *
 * <p>{@link autodispose2.lifecycle.LifecycleScopeProvider} is a special case targeted at binding to
 * things with lifecycles. Its API is as follows: - {@link
 * autodispose2.lifecycle.LifecycleScopeProvider#lifecycle()} - returns an {@link
 * io.reactivex.Observable} of lifecycle events. This should be backed by a {@link
 * io.reactivex.subjects.BehaviorSubject} or something similar ({@code BehaviorRelay}, etc). -
 * {@link autodispose2.lifecycle.LifecycleScopeProvider#correspondingEvents()} - a mapping of events
 * to corresponding ones, i.e. Attach -> Detach. - {@link
 * autodispose2.lifecycle.LifecycleScopeProvider#peekLifecycle()} - returns the current lifecycle
 * state of the object.
 *
 * <p>In {@link autodispose2.ScopeProvider#requestScope()}, the implementation expects to these
 * pieces to construct a {@link io.reactivex.CompletableSource} representation of the proper end
 * scope, while also doing precondition checks for lifecycle boundaries. If a lifecycle has not
 * started, it will send you to {@code onError} with a {@link
 * autodispose2.lifecycle.LifecycleNotStartedException}. If the lifecycle as ended, it is
 * recommended to throw a {@link autodispose2.lifecycle.LifecycleEndedException} in your {@link
 * autodispose2.lifecycle.LifecycleScopeProvider#correspondingEvents() correspondingEvents()}
 * mapping, but it is up to the user.
 *
 * <p>To simplify implementations, there's an included {@link
 * autodispose2.lifecycle.LifecycleScopes} utility class with factories for generating {@link
 * io.reactivex.CompletableSource} representations from {@link
 * autodispose2.lifecycle.LifecycleScopeProvider} instances.
 */
package autodispose2.lifecycle;
