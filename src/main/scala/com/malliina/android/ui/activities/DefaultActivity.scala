package com.malliina.android.ui.activities

import com.malliina.android.ui.ActivityUtils
import android.app.Activity

trait DefaultActivity extends BaseActivity with LayoutActivity with ActivityUtils {
  override def activity: Activity = this

  def extras = Option(getIntent.getExtras)
}
