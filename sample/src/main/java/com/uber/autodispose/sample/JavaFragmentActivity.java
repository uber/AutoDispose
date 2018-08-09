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

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import static com.uber.autodispose.AutoDispose.autoDisposable;

/**
 * This leverages the Architecture Components support for the demo.
 */

public class JavaFragmentActivity extends AppCompatActivity {
    private static final String TAG = "AutoDispose";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new JavaFragment())
                    .commit();
        }
    }

    public static class JavaFragment extends Fragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Log.d(TAG, "onCreate()");

            // Using automatic disposal, this should determine that the correct time to
            // dispose is onDestroy (the opposite of onCreate).
            Observable.interval(1, TimeUnit.SECONDS)
                    .doOnDispose(() -> Log.i(TAG, "Disposing subscription from onCreate()"))
                    .as(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                    .subscribe(num -> Log.i(TAG, "Started in onCreate(), running until onDestroy(): " + num));
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView tv = new TextView(getContext());
            tv.setText("JavaFragment");
            return tv;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Observable.interval(1, TimeUnit.SECONDS)
                    .doOnDispose(() -> Log.i(TAG, "Disposing subscription from onDestroyView()"))
                    .as(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                    .subscribe(num -> Log.i(TAG, "Started in onActivityCreated(), running until onDestroyView(): " + num));
        }

        @Override
        public void onStart() {
            super.onStart();
            Log.d(TAG, "onStart()");

            // Using automatic disposal, this should determine that the correct time to
            // dispose is onStop (the opposite of onStart).
            Observable.interval(1, TimeUnit.SECONDS)
                    .doOnDispose(() -> Log.i(TAG, "Disposing subscription from onStart()"))
                    .as(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
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
                    .as(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                    .subscribe(num -> Log.i(TAG, "Started in onResume(), running until in onPause(): " + num));

            // Setting a specific untilEvent, this should dispose in onDestroy.
            Observable.interval(1, TimeUnit.SECONDS)
                    .doOnDispose(() -> Log.i(TAG, "Disposing subscription from onResume() with untilEvent ON_DESTROY"))
                    .as(autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                    .subscribe(num -> Log.i(TAG, "Started in onResume(), running until in onDestroy(): " + num));
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
        public void onDestroy() {
            Log.d(TAG, "onDestroy()");
            super.onDestroy();
        }

        @Override
        public void onDestroyView() {
            Log.d(TAG, "onDestroyView()");
            super.onDestroyView();
        }
    }
}