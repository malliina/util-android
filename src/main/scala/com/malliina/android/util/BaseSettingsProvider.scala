package com.malliina.android.util

import android.content.SharedPreferences
import play.api.libs.json.{Reads, Writes}
import play.api.libs.json.Json._
import PreferenceImplicits._

trait BaseSettingsProvider {
  def prefs: SharedPreferences

  def loadStrings(key: String) = loadSeq[String](key)

  def save[T](key: String, values: Seq[T])(implicit tjs: Writes[T]): Unit =
    savePref(key, stringify(toJson(values)))

  def savePref(key: String, value: String): Unit =
    prefs.put(key, value)

  def loadSeqOrEmpty[T](key: String)(implicit tjs: Reads[T]) =
    loadSeq(key) getOrElse Seq.empty

  def loadSeq[T](key: String)(implicit tjs: Reads[T]): Option[Seq[T]] =
    loadString(key).map(str => parse(str).asOpt[Seq[T]].getOrElse(Seq.empty[T]))

  def loadString(key: String): Option[String] = Option(prefs.getString(key, null))
}
