/*
 * Copyright 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package autodispose2;

import io.reactivex.rxjava3.disposables.Disposable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility methods for working with Disposables atomically. Copied from the RxJava implementation.
 */
enum AutoDisposableHelper implements Disposable {
  /**
   * The singleton instance representing a terminal, disposed state, don't leak it.
   */
  DISPOSED;

  /**
   * Atomically disposes the Disposable in the field if not already disposed.
   *
   * @param field the target field
   * @return true if the current thread managed to dispose the Disposable
   */
  static boolean dispose(AtomicReference<Disposable> field) {
    Disposable current = field.get();
    Disposable d = DISPOSED;
    if (current != d) {
      current = field.getAndSet(d);
      if (current != d) {
        if (current != null) {
          current.dispose();
        }
        return true;
      }
    }
    return false;
  }

  @Override public void dispose() {
    // deliberately no-op
  }

  @Override public boolean isDisposed() {
    return true;
  }
}
