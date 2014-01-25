package com.mle.android.iap.amazon

import com.mle.android.iap.IapUtilsBase
import android.app.Activity
import scala.concurrent.Future
import com.amazon.inapp.purchasing.{Offset, PurchasingManager}
import com.mle.util.Utils
import Utils.executionContext

/**
 *
 * @author mle
 */
trait AmazonIapUtils extends IapUtilsBase {
  def skus(activity: Activity): Future[Set[String]] = {
    val observer = new AsyncAmazonIapHelper(activity)
    observer.userId.flatMap(_ => {
      PurchasingManager.initiatePurchaseUpdatesRequest(Offset.BEGINNING)
      observer.entitledSkus
    })
  }

  def hasSku(sku: String, activity: Activity): Future[Boolean] =
    skus(activity).map(_ contains sku)

  def purchase(sku: String, activity: Activity): Future[String] = {
    val observer = new AsyncAmazonIapHelper(activity)
    observer.userId.flatMap(_ => {
      PurchasingManager.initiatePurchaseRequest(sku)
      observer.purchase
    })
  }
}

object AmazonIapUtils extends AmazonIapUtils
