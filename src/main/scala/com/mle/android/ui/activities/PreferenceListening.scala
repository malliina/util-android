package com.mle.android.ui.activities

import android.app.Activity
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.mle.android.ui.ActivityUtils

/**
 *
 * @author mle
 */
trait PreferenceListening extends Activity with ActivityUtils with OnSharedPreferenceChangeListener {

  override def onResume() {
    prefs registerOnSharedPreferenceChangeListener this
    super.onResume()
  }

  override def onPause() {
    super.onPause()
    prefs unregisterOnSharedPreferenceChangeListener this
  }
}
