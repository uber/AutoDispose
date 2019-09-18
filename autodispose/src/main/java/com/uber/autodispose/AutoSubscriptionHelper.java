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

package com.uber.autodispose;

import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscription;

/**
 * Utility methods to validate Subscriptions in the various onSubscribe calls. Copied from the
 * RxJava implementation.
 */
enum AutoSubscriptionHelper implements Subscription {
  /**
   * Represents a cancelled Subscription.
   * <p>Don't leak this instance!
   */
  CANCELLED;

  @Override public void request(long n) {
    // deliberately ignored
  }

  @Override public void cancel() {
    // deliberately ignored
  }

  /**
   * Verifies that current is null, next is not null, otherwise signals errors
   * to the RxJavaPlugins and returns false.
   *
   * @param current the current Subscription, expected to be null
   * @param next the next Subscription, expected to be non-null
   * @return true if the validation succeeded
   */
  static boolean validate(@Nullable Subscription current, Subscription next) {
    //noinspection ConstantConditions left as is from original RxJava implementation
    if (next == null) {
      RxJavaPlugins.onError(new NullPointerException("next is null"));
      return false;
    }
    if (current != null) {
      next.cancel();
      reportSubscriptionSet();
      return false;
    }
    return true;
  }

  /**
   * Reports that the subscription is already set to the RxJavaPlugins error handler,
   * which is an indication of a onSubscribe management bug.
   */
  static void reportSubscriptionSet() {
    RxJavaPlugins.onError(new IllegalStateException("Subscription already set!"));
  }

  /**
   * Validates that the n is positive.
   *
   * @param n the request amount
   * @return false if n is non-positive.
   */
  static boolean validate(long n) {
    if (n <= 0) {
      RxJavaPlugins.onError(new IllegalArgumentException("n > 0 required but it was " + n));
      return false;
    }
    return true;
  }

  /**
   * Reports to the plugin error handler that there were more values produced than requested, which
   * is a sign of internal backpressure handling bug.
   *
   * @param n the overproduction amount
   */
  static void reportMoreProduced(long n) {
    RxJavaPlugins.onError(new IllegalStateException("More produced than requested: " + n));
  }

  /**
   * Check if the given subscription is the common cancelled subscription.
   *
   * @param s the subscription to check
   * @return true if the subscription is the common cancelled subscription
   */
  static boolean isCancelled(Subscription s) {
    return s == CANCELLED;
  }

  /**
   * Atomically sets the subscription on the field and cancels the
   * previous subscription if any.
   *
   * @param field the target field to set the new subscription on
   * @param s the new subscription
   * @return true if the operation succeeded, false if the target field
   * holds the {@link #CANCELLED} instance.
   * @see #replace(AtomicReference, Subscription)
   */
  static boolean set(AtomicReference<Subscription> field, @Nullable Subscription s) {
    for (; ; ) {
      Subscription current = field.get();
      if (current == CANCELLED) {
        if (s != null) {
          s.cancel();
        }
        return false;
      }
      if (field.compareAndSet(current, s)) {
        if (current != null) {
          current.cancel();
        }
        return true;
      }
    }
  }

  /**
   * Atomically sets the subscription on the field if it is still null.
   * <p>If the field is not null and doesn't contain the {@link #CANCELLED}
   * instance, the {@link #reportSubscriptionSet()} is called.
   *
   * @param field the target field
   * @param s the new subscription to set
   * @return true if the operation succeeded, false if the target field was not null.
   */
  static boolean setOnce(AtomicReference<Subscription> field, Subscription s) {
    AutoDisposeUtil.checkNotNull(s, "s is null");
    if (!field.compareAndSet(null, s)) {
      s.cancel();
      if (field.get() != CANCELLED) {
        reportSubscriptionSet();
      }
      return false;
    }
    return true;
  }

  /**
   * Atomically sets the field to the given non-null Subscription and returns true
   * or returns false if the field is non-null.
   *
   * @param field the target field
   * @param s the subscription to set, not null
   * @return true if the operation succeeded, false
   */
  static boolean setIfNotSet(AtomicReference<Subscription> field, Subscription s) {
    AutoDisposeUtil.checkNotNull(s, "s is null");
    return field.compareAndSet(null, s);
  }

  /**
   * Atomically sets the subscription on the field but does not
   * cancel the previous subscription.
   *
   * @param field the target field to set the new subscription on
   * @param s the new subscription
   * @return true if the operation succeeded, false if the target field
   * holds the {@link #CANCELLED} instance.
   * @see #set(AtomicReference, Subscription)
   */
  static boolean replace(AtomicReference<Subscription> field, @Nullable Subscription s) {
    for (; ; ) {
      Subscription current = field.get();
      if (current == CANCELLED) {
        if (s != null) {
          s.cancel();
        }
        return false;
      }
      if (field.compareAndSet(current, s)) {
        return true;
      }
    }
  }

  /**
   * Atomically swaps in the common cancelled subscription instance
   * and cancels the previous subscription if any.
   *
   * @param field the target field to dispose the contents of
   * @return true if the swap from the non-cancelled instance to the
   * common cancelled instance happened in the caller's thread (allows
   * further one-time actions).
   */
  static boolean cancel(AtomicReference<Subscription> field) {
    Subscription current = field.get();
    if (current != CANCELLED) {
      current = field.getAndSet(CANCELLED);
      if (current != CANCELLED) {
        if (current != null) {
          current.cancel();
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Atomically sets the new Subscription on the field and requests any accumulated amount
   * from the requested field.
   *
   * @param field the target field for the new Subscription
   * @param requested the current requested amount
   * @param s the new Subscription, not null (verified)
   * @return true if the Subscription was set the first time
   */
  static boolean deferredSetOnce(AtomicReference<Subscription> field, AtomicLong requested, Subscription s) {
    if (AutoSubscriptionHelper.setOnce(field, s)) {
      long r = requested.getAndSet(0L);
      if (r != 0L) {
        s.request(r);
      }
      return true;
    }
    return false;
  }

  /**
   * Atomically requests from the Subscription in the field if not null, otherwise accumulates
   * the request amount in the requested field to be requested once the field is set to non-null.
   *
   * @param field the target field that may already contain a Subscription
   * @param requested the current requested amount
   * @param n the request amount, positive (verified)
   */
  static void deferredRequest(AtomicReference<Subscription> field, AtomicLong requested, long n) {
    Subscription s = field.get();
    if (s != null) {
      s.request(n);
    } else {
      if (AutoSubscriptionHelper.validate(n)) {
        AutoDisposeBackpressureHelper.add(requested, n);

        s = field.get();
        if (s != null) {
          long r = requested.getAndSet(0L);
          if (r != 0L) {
            s.request(r);
          }
        }
      }
    }
  }
}
