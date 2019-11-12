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
package autodispose2.android;

import android.view.View;
import autodispose2.ScopeProvider;
import io.reactivex.rxjava3.core.CompletableSource;

/**
 * A {@link ScopeProvider} that can provide scoping for Android {@link View} classes.
 *
 * <p>
 *
 * <pre><code>
 *   AutoDispose.autoDisposable(ViewScopeProvider.from(view));
 * </code></pre>
 */
public final class ViewScopeProvider implements ScopeProvider {

  private final View view;

  /**
   * Creates a {@link ScopeProvider} for Android Views.
   *
   * @param view the view to scope for
   * @return a {@link ScopeProvider} against this view.
   */
  public static ScopeProvider from(View view) {
    if (view == null) {
      throw new NullPointerException("view == null");
    }
    return new ViewScopeProvider(view);
  }

  private ViewScopeProvider(final View view) {
    this.view = view;
  }

  @Override
  public CompletableSource requestScope() {
    return new DetachEventCompletable(view);
  }
}
