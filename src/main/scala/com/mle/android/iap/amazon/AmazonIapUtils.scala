package com.mle.android.iap.amazon

import android.app.Activity
import com.amazon.inapp.purchasing.{Offset, PurchasingManager}
import com.mle.android.iap.{IapUtilsBase, ProductInfo}
import com.mle.concurrent.ExecutionContexts.cached

import scala.concurrent.Future

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
    import scala.collection.JavaConversions._
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
