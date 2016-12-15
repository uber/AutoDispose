package com.uber.autodispose;

/**
 * Signifies an error occurred due to execution starting after the lifecycle has ended.
 */
public class LifecycleEndedException extends OutsideLifecycleException {

  public LifecycleEndedException() {
    this("Lifecycle has ended!");
  }

  public LifecycleEndedException(String s) {
    super(s);
  }
}
