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

package com.uber.autodispose.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.concurrent.TimeUnit;

/**
 * Demo activity, shamelessly borrowed from the RxLifecycle sample
 * <p>
 * This leverages the Architecture Components support for the demo
 */
public class MainActivity extends AppCompatActivity {

  private static final String TAG = "AutoDispose";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "onCreate()");

    setContentView(R.layout.activity_main);

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onCreate (the opposite of onStop).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose(new Action() {
          @Override public void run() throws Exception {
            Log.i(TAG, "Disposing subscription from onCreate()");
          }
        })
        .to(AutoDispose.with(AndroidLifecycleScopeProvider.from(this)).<Long>forObservable())
        .subscribe(new Consumer<Long>() {
          @Override public void accept(Long num) throws Exception {
            Log.i(TAG, "Started in onCreate(), running until onDestroy(): " + num);
          }
        });
  }

  @Override protected void onStart() {
    super.onStart();

    Log.d(TAG, "onStart()");

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onStop (the opposite of onStart).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose(new Action() {
          @Override public void run() throws Exception {
            Log.i(TAG, "Disposing subscription from onStart()");
          }
        })
        .to(AutoDispose.with(AndroidLifecycleScopeProvider.from(this)).<Long>forObservable())
        .subscribe(new Consumer<Long>() {
          @Override public void accept(Long num) throws Exception {
            Log.i(TAG, "Started in onStart(), running until in onStop(): " + num);
          }
        });
  }

  @Override protected void onResume() {
    super.onResume();

    Log.d(TAG, "onResume()");

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onPause (the opposite of onResume).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose(new Action() {
          @Override public void run() throws Exception {
            Log.i(TAG, "Disposing subscription from onResume()");
          }
        })
        // `.<Long>forObservable` is necessary if you're compiling on JDK7 or below.
        // If you're using JDK8+, then you can safely remove it.
        .to(AutoDispose.with(AndroidLifecycleScopeProvider.from(this)).<Long>forObservable())
        .subscribe(new Consumer<Long>() {
          @Override public void accept(Long num) throws Exception {
            Log.i(TAG, "Started in onResume(), running until in onDestroy(): " + num);
          }
        });
  }

  @Override protected void onPause() {
    Log.d(TAG, "onPause()");
    super.onPause();
  }

  @Override protected void onStop() {
    Log.d(TAG, "onStop()");
    super.onStop();
  }

  @Override protected void onDestroy() {
    Log.d(TAG, "onDestroy()");
    super.onDestroy();
  }
}
