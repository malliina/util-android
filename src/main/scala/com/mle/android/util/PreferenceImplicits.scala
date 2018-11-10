package com.mle.android.util

import android.content.SharedPreferences

object PreferenceImplicits {

  implicit class RichPrefs(val pref: SharedPreferences) extends AnyVal {
    def get(key: String): Option[String] = Option(pref.getString(key, null))

    def put(key: String, value: String): Unit = withEditor(_.putString(key, value))

    def putInt(key: String, value: Int): Unit = withEditor(_.putInt(key, value))

    def putBoolean(key: String, value: Boolean): Unit = withEditor(_.putBoolean(key, value))

    def withEditor(f: SharedPreferences.Editor => SharedPreferences.Editor): Unit =
      f(pref.edit()).apply()
  }

}
