package com.mle.android.ui.activities

import android.app.Activity
import android.os.Bundle

/**
 *
 * @author mle
 */
trait LayoutActivity extends Activity {

  def contentView: Int

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(contentView)
  }
}