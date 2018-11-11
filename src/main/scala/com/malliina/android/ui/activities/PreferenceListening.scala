package com.malliina.android.ui.activities

import android.app.Activity
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.malliina.android.ui.ActivityUtils

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
