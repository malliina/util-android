package com.mle.android.util

import android.content.SharedPreferences

/**
 *
 * @author mle
 */
object PreferenceImplicits {

  implicit class RichPrefs(val pref: SharedPreferences) extends AnyVal {
    def get(key: String): Option[String] = Option(pref.getString(key, null))
  }
}
