package com.uber.autodispose.android;

import android.os.Looper;

class Util {
  static boolean isMainThread() {
    try {
      return Looper.myLooper() == Looper.getMainLooper();
    } catch (Exception e) {
      // Cover for tests
      return true;
    }
  }
}
