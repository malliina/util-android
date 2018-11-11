package com.malliina.android.iap

import android.app.Activity
import scala.concurrent.Future
import com.malliina.android.util.UtilLog
import com.malliina.android.exceptions.AndroidException

object IapUtilsBase {
  val purchaseFailedMessage = "The purchase did not complete successfully."
  val invalidSkuMessage = "The product ID does not exist. Try again later."
  val alreadyPurchasedMessage = "The product has already been purchased."
  val purchaseCanceledMessage = "The purchase was canceled."
}

trait IapUtilsBase extends UtilLog {
  def hasSku(sku: String, activity: Activity): Future[Boolean]

  /**
    * @param sku      product ID
    * @param activity context
    * @return the details of `sku` or a failure with [[NoSuchElementException]] if `sku` does not exist
    */
  def productInfo(sku: String, activity: Activity): Future[ProductInfo]

  /** Initiates the purchase of `sku`.
    *
    * The returned [[Future]] fails with an [[IapException]], for example
    * [[InvalidSkuException]], [[AlreadyPurchasedException]] or
    * [[PurchaseCanceledException]] if the purchase is not completed successfully.
    *
    * @param sku      product id
    * @param activity context
    * @return the purchased SKU
    */
  def purchase(sku: String, activity: Activity): Future[String]

  protected def logException(msg: String): PartialFunction[Throwable, Unit] = {
    case t: Throwable => warn(msg, t)
  }
}

class IapException(msg: String, t: Option[Throwable] = None) extends AndroidException(msg, t)

class InvalidSkuException extends IapException(IapUtilsBase.invalidSkuMessage)

class AlreadyPurchasedException extends IapException(IapUtilsBase.alreadyPurchasedMessage)

class PurchaseCanceledException extends IapException(IapUtilsBase.purchaseCanceledMessage)
