package com.malliina.android.ui.activities

import android.app.Activity
import android.os.Bundle

trait LayoutActivity extends Activity {

  def contentView: Int

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(contentView)
  }
}