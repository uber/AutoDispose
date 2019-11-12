/**
 * Copyright 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package autodispose2;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class to help with backpressure-related operations such as request aggregation. Copied
 * from the RxJava implementation.
 */
final class AutoDisposeBackpressureHelper {
  /** Utility class. */
  private AutoDisposeBackpressureHelper() {
    throw new IllegalStateException("No instances!");
  }

  /**
   * Adds two long values and caps the sum at Long.MAX_VALUE.
   *
   * @param a the first value
   * @param b the second value
   * @return the sum capped at Long.MAX_VALUE
   */
  private static long addCap(long a, long b) {
    long u = a + b;
    if (u < 0L) {
      return Long.MAX_VALUE;
    }
    return u;
  }

  /**
   * Atomically adds the positive value n to the requested value in the AtomicLong and
   * caps the result at Long.MAX_VALUE and returns the previous value.
   *
   * @param requested the AtomicLong holding the current requested value
   * @param n the value to add, must be positive (not verified)
   * @return the original value before the add
   */
  static long add(AtomicLong requested, long n) {
    for (; ; ) {
      long r = requested.get();
      if (r == Long.MAX_VALUE) {
        return Long.MAX_VALUE;
      }
      long u = addCap(r, n);
      if (requested.compareAndSet(r, u)) {
        return r;
      }
    }
  }
}
