package com.mle.android.util

import android.content.Context
import android.preference.PreferenceManager
import play.api.libs.json.Json._
import com.mle.android.util.PreferenceImplicits._
import play.api.libs.json.Format

class PersistentList[T](ctx: Context, key: String, maxSize: Int = 100)(implicit val format: Format[T]) {
  def prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

  private var inner: List[T] = loadSavedList getOrElse List.empty[T]

  def prepend(elem: T) = {
    inner = elem :: inner
    if (inner.size > maxSize) {
      inner = inner take maxSize
    }
    prefs.edit().putString(key, stringify(toJson(inner))).apply()
  }

  def get: List[T] = inner

  private def loadSavedList =
    for {
      str <- prefs get key
      listOpt <- parse(str).asOpt[List[T]]
    } yield listOpt
}
