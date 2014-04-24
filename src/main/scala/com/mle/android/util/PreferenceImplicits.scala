package com.mle.android.util

import android.content.SharedPreferences

/**
 *
 * @author mle
 */
object PreferenceImplicits {

  implicit class RichPrefs(val pref: SharedPreferences) extends AnyVal {
    def get(key: String): Option[String] = Option(pref.getString(key, null))

    def put(key: String, value: String) = withEditor(_.putString(key, value))

    def putInt(key: String, value: Int) = withEditor(_.putInt(key, value))

    def withEditor(f: SharedPreferences.Editor => SharedPreferences.Editor) =
      f(pref.edit()).apply()
  }

}
