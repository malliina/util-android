package com.mle.android.ui

import android.app.Activity
import android.preference.PreferenceManager
import android.content.Intent
import android.widget.Toast
import com.mle.util.Utils

/**
 *
 * @author mle
 */
trait ActivityUtils {
  def activity: Activity

  def findActivity = Option(activity)

  def prefs = PreferenceManager.getDefaultSharedPreferences(activity)

  def tryFindView[A](id: Int): Option[A] =
    for {
      a <- findActivity
      view <- Option(a findViewById id)
    } yield view.asInstanceOf[A]

  def navigate[T <: Activity](destActivity: Class[T], parameters: (String, String)*) {
    val intent = new Intent(activity, destActivity)
    parameters foreach {
      case (key, value) => intent.putExtra(key, value)
    }
    activity startActivity intent
  }

  def navigateForResult[T <: Activity](destActivity: Class[T], requestCode: Int) {
    val intent = new Intent(activity, destActivity)
    activity startActivityForResult(intent, requestCode)
  }

  def onUiThread(f: => Any): Unit = activity.runOnUiThread(Utils.runnable(f))


  def showToast(text: String, duration: Int = Toast.LENGTH_LONG): Unit =
    onUiThread(Toast.makeText(activity, text, duration).show())

  def showToast(stringRes: Int): Unit = findActivity
    .map(_.getResources.getString(stringRes))
    .foreach(str => showToast(str))
}
