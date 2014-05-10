package com.mle.android.ui.activities

import com.mle.android.ui.ActivityUtils
import android.app.Activity

/**
 * @author Michael
 */
trait DefaultActivity extends BaseActivity with LayoutActivity with ActivityUtils {
  override def activity: Activity = this

  def extras = Option(getIntent.getExtras)
}
