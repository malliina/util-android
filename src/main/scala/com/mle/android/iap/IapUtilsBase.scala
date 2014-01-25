package com.mle.android.iap

import android.app.Activity
import scala.concurrent.Future
import com.mle.android.util.UtilLog

/**
 *
 * @author mle
 */
trait IapUtilsBase extends UtilLog {
  def hasSku(sku: String, activity: Activity): Future[Boolean]

  def purchase(sku: String, activity: Activity): Future[String]

  protected def logException(msg: String): PartialFunction[Throwable, Unit] = {
    case t: Throwable => warn(msg, t)
  }
}