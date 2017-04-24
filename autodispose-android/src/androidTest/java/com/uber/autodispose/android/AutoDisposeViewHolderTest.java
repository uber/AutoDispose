/*
 * Copyright (C) 2017. Uber Technologies
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

package com.uber.autodispose.android;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.uber.autodispose.ObservableScoper;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;

/**
 * Bit of an odd one, but this test works by flinging the list and ensuring the number of recycles
 * lines up with the number of disposals. We can't monitor recycling + immediate disposable directly
 * because it's actually async.
 */
@RunWith(AndroidJUnit4.class) public final class AutoDisposeViewHolderTest {
  @Rule public final ActivityTestRule<AutoDisposeViewHolderTestActivity> activityRule =
      new ActivityTestRule<>(AutoDisposeViewHolderTestActivity.class);

  private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
  private final AtomicInteger recycledCounter = new AtomicInteger();
  private final AtomicInteger disposeCounter = new AtomicInteger();

  private RecyclerView view;
  private ViewInteraction interaction;
  private ViewDirtyIdlingResource viewDirtyIdler;

  @Before public void setUp() {
    AutoDisposeViewHolderTestActivity activity = activityRule.getActivity();
    view = activity.recyclerView;
    interaction = onView(withId(android.R.id.primary));
    viewDirtyIdler = new ViewDirtyIdlingResource(activity);
    Espresso.registerIdlingResources(viewDirtyIdler);
  }

  @After public void tearDown() {
    Espresso.unregisterIdlingResources(viewDirtyIdler);
  }

  @Test public void basicTest() {
    final Adapter adapter = new Adapter();
    final RecyclerView.RecyclerListener delegateListener =
        AutoDisposeViewHolder.newRecyclerListener();
    view.setRecyclerListener(new RecyclerView.RecyclerListener() {
      @Override public void onViewRecycled(RecyclerView.ViewHolder h) {
        delegateListener.onViewRecycled(h);
        recycledCounter.incrementAndGet();
      }
    });

    instrumentation.runOnMainSync(new Runnable() {
      @Override public void run() {
        view.setAdapter(adapter);
      }
    });

    interaction.perform(swipeUp());
    instrumentation.waitForIdleSync();
    int disposeCount = disposeCounter.get();
    int recycledCount = recycledCounter.get();
    Log.d("TEST", "Counts after swipe up are " + disposeCount + " and " + recycledCount);
    assertThat(disposeCount).isGreaterThan(0);
    assertThat(recycledCount).isGreaterThan(0);
    assertThat(disposeCount).isEqualTo(recycledCount);

    // Swipe back down to confirm rebinding also behaves correctly.
    interaction.perform(swipeDown());
    instrumentation.waitForIdleSync();
    disposeCount = disposeCounter.get();
    recycledCount = recycledCounter.get();
    Log.d("TEST", "Counts after swipe down are " + disposeCount + " and " + recycledCount);
    assertThat(disposeCount).isGreaterThan(0);
    assertThat(recycledCount).isGreaterThan(0);
    assertThat(disposeCount).isEqualTo(recycledCount);
  }

  private class Adapter extends RecyclerView.Adapter<ViewHolder> {

    public Adapter() {
      setHasStableIds(true);
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
      TextView v = (TextView) LayoutInflater.from(parent.getContext())
          .inflate(android.R.layout.simple_list_item_1, parent, false);
      return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      holder.textView.setText(String.valueOf(position));

      holder.disposable = Observable.never()
          .doOnDispose(new Action() {
            @Override public void run() throws Exception {
              disposeCounter.incrementAndGet();
            }
          })
          .to(new ObservableScoper<>(holder))
          .subscribe();
    }

    @Override public int getItemCount() {
      return 100;
    }

    @Override public long getItemId(int position) {
      return position;
    }
  }

  private static class ViewHolder extends AutoDisposeViewHolder {

    Disposable disposable;
    TextView textView;

    ViewHolder(TextView itemView) {
      super(itemView);
      this.textView = itemView;
    }
  }
}
