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
package com.uber.autodispose.sample;

import static com.uber.autodispose.AutoDispose.autoDisposable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.core.Observable;
import java.util.concurrent.TimeUnit;

/**
 * Demo Fragment showing both conventional lifecycle management as well as the new {@link
 * #getViewLifecycleOwner()} API.
 *
 * <p>This leverages the Architecture Components support for the demo.
 */
public class JavaFragment extends Fragment {

  private static final String TAG = "JavaFragment";

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate()");

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onDestroy (the opposite of onCreate).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose(() -> Log.i(TAG, "Disposing subscription from onCreate()"))
        .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
        .subscribe(num -> Log.i(TAG, "Started in onCreate(), running until onDestroy(): " + num));
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView()");
    return inflater.inflate(R.layout.content_main, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Log.d(TAG, "onViewCreated()");
    // Using automatic disposal, this should determine that the correct time to
    // dispose is onDestroyView (the opposite of onCreateView).
    // Note we do this in onViewCreated to defer until after the view is created
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose(() -> Log.i(TAG, "Disposing subscription from onViewCreated()"))
        .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
        .subscribe(
            num -> Log.i(TAG, "Started in onViewCreated(), running until onDestroyView(): " + num));
  }

  @Override
  public void onStart() {
    super.onStart();

    Log.d(TAG, "onStart()");

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onStop (the opposite of onStart).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose(() -> Log.i(TAG, "Disposing subscription from onStart()"))
        .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
        .subscribe(num -> Log.i(TAG, "Started in onStart(), running until in onStop(): " + num));
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "onResume()");

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onPause (the opposite of onResume).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose(() -> Log.i(TAG, "Disposing subscription from onResume()"))
        .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
        .subscribe(num -> Log.i(TAG, "Started in onResume(), running until in onPause(): " + num));

    // Setting a specific untilEvent, this should dispose in onDestroy.
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose(
            () -> Log.i(TAG, "Disposing subscription from onResume() with untilEvent ON_DESTROY"))
        .to(autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
        .subscribe(
            num -> Log.i(TAG, "Started in onResume(), running until in onDestroy(): " + num));
  }

  @Override
  public void onPause() {
    Log.d(TAG, "onPause()");
    super.onPause();
  }

  @Override
  public void onStop() {
    Log.d(TAG, "onStop()");
    super.onStop();
  }

  @Override
  public void onDestroyView() {
    Log.d(TAG, "onDestroyView()");
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy()");
    super.onDestroy();
  }
}
