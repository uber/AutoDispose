package com.uber.autodispose.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

/**
 * Central activity that opens up other activities.
 */
class HomeActivity: AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    val mainActivityButton: Button = findViewById(R.id.mainActivityButton)
    mainActivityButton.setOnClickListener {
      startActivity(MainActivity::class.java)
    }

    val archComponentsButton: Button = findViewById(R.id.archComponentsActivityButton)
    archComponentsButton.setOnClickListener {
      startActivity(ArchComponentActivity::class.java)
    }

    val kotlinActivityButton: Button = findViewById(R.id.kotlinActivityButton)
    kotlinActivityButton.setOnClickListener {
      startActivity(KotlinActivity::class.java)
    }
  }

  private fun startActivity(className: Class<*>) {
    val intent = Intent(this, className)
    startActivity(intent)
  }
}
