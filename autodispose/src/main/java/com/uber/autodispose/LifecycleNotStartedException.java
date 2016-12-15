package com.uber.autodispose;

/**
 * Signifies an error occurred due to execution starting before the lifecycle has started.
 */
public class LifecycleNotStartedException extends OutsideLifecycleException {

  public LifecycleNotStartedException() {
    this("Lifecycle hasn't started!");
  }

  public LifecycleNotStartedException(String s) {
    super(s);
  }
}
