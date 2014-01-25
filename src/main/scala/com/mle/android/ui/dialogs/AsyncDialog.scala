package com.mle.android.ui.dialogs

import android.support.v4.app.{FragmentManager, DialogFragment}
import scala.concurrent._

/**
 * This does not work well when a user rotates the screen while the dialog is open: Android
 * ensures the dialog itself is shown, but any code waiting for the future to complete will
 * wait a very long time.
 *
 * @author mle
 */
trait AsyncDialog extends DialogFragment {
  protected val prom = promise[Boolean]()

  val result = prom.future

  def onPositive = prom success true

  def onNegative = prom success false

  /**
   *
   * @param fm fragment manager passed on to `show`
   * @param id id passed on to `show`
   * @return a future that completes successfully to true/false depending on the user's choice (yes/no)
   */
  def showAsync(fm: FragmentManager, id: String): Future[Boolean] = {
    show(fm, id)
    result
  }
}
