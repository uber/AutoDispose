/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package autodispose2.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/** Central activity that opens up other activities. */
class HomeActivity : AppCompatActivity() {

  lateinit var toolbar: Toolbar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    toolbar = findViewById(R.id.toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.let { setTitle(R.string.app_name) }

    val mainActivityButton: Button = findViewById(R.id.javaActivityButton)
    mainActivityButton.setOnClickListener { startActivity(JavaActivity::class.java) }

    val archComponentsButton: Button = findViewById(R.id.archComponentsActivityButton)
    archComponentsButton.setOnClickListener { startActivity(ArchComponentActivity::class.java) }

    val kotlinActivityButton: Button = findViewById(R.id.kotlinActivityButton)
    kotlinActivityButton.setOnClickListener { startActivity(KotlinActivity::class.java) }

    val disposingActivityButton: Button = findViewById(R.id.disposingActivity)
    disposingActivityButton.setOnClickListener {
      startActivity(DisposingViewModelActivity::class.java)
    }
  }

  private fun startActivity(className: Class<*>) {
    val intent = Intent(this, className)
    startActivity(intent)
  }
}
