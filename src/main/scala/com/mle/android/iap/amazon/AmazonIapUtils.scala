package com.mle.android.iap.amazon

import com.mle.android.iap.{ProductInfo, IapUtilsBase}
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
  def skus(activity: Activity): Future[Set[String]] = withIAP(activity, iap => {
    PurchasingManager.initiatePurchaseUpdatesRequest(Offset.BEGINNING)
    iap.entitledSkus
  })

  def hasSku(sku: String, activity: Activity): Future[Boolean] =
    skus(activity).map(_ contains sku)

  override def productInfo(sku: String, activity: Activity): Future[ProductInfo] = withIAP(activity, iap => {
    import collection.JavaConversions._
    PurchasingManager.initiateItemDataRequest(Set(sku))
    iap.availableItems.flatMap(skus => skus.headOption.fold(Future.failed[ProductInfo](new NoSuchElementException))(s => Future.successful(s)))
  })

  def purchase(sku: String, activity: Activity): Future[String] = withIAP(activity, iap => {
    PurchasingManager.initiatePurchaseRequest(sku)
    iap.purchase
  })

  private def withIAP[T](activity: Activity, f: AsyncAmazonIapHelper => Future[T]): Future[T] = {
    val observer = new AsyncAmazonIapHelper(activity)
    observer.userId.flatMap(_ => f(observer))
  }
}

object AmazonIapUtils extends AmazonIapUtils
