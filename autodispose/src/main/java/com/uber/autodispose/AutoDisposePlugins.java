/*
 * Copyright (c) 2017. Uber Technologies
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

package com.uber.autodispose;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Utility class to inject handlers to certain standard AutoDispose operations.
 */
public class AutoDisposePlugins {

    @Nullable
    static volatile Consumer<? super OutsideLifecycleException> outsideLifecycleHandler;

    /**
     * Prevents changing the plugins.
     */
    static volatile boolean lockdown;

    /**
     * Prevents changing the plugins from then on.
     * <p>This allows container-like environments to prevent client messing with plugins.</p>
     */
    public static void lockdown() {
        lockdown = true;
    }

    /**
     * Returns true if the plugins were locked down.
     *
     * @return true if the plugins were locked down
     */
    public static boolean isLockdown() {
        return lockdown;
    }

    /**
     * @return the consumer for handling {@link OutsideLifecycleException}.
     */
    @Nullable
    public static Consumer<? super OutsideLifecycleException> getOutsideLifecycleHandler() {
        return outsideLifecycleHandler;
    }

    /**
     * @param handler the consumer for handling {@link OutsideLifecycleException} to set, null allowed
     */
    public static void setOutsideLifecycleHandler(@Nullable Consumer<? super OutsideLifecycleException> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        outsideLifecycleHandler = handler;
    }

    /**
     * Called when an outside lifecycle error occurs.
     * @param error the error to report
     */
    public static void onOutsideLifecycleException(@NonNull OutsideLifecycleException error) {
        Consumer<? super  OutsideLifecycleException> f = outsideLifecycleHandler;

        if (error == null) {
            RxJavaPlugins.onError(
                    new NullPointerException("onOutsideLifecycleException called with null. Null values are generally "
                    + "not allowed in 2.x operators and sources.")
            );
        }

        if (f != null) {
            try {
                f.accept(error);
                return;
            } catch (Throwable e) {
                RxJavaPlugins.onError(e);
            }
        }
    }

    /**
     * Removes all handlers and resets to default behavior.
     */
    public static void reset() {
        setOutsideLifecycleHandler(null);
    }
}
