package com.mle.android.iap.google

import android.app.Activity
import com.android.iab.util.IabHelper
import com.mle.android.iap.{IapUtilsBase, ProductInfo}
import com.mle.concurrent.ExecutionContexts.cached

import scala.concurrent.Future
import scala.util.Try

/**
 *
 * @author mle
 */
trait GoogleIapUtils extends IapUtilsBase {
  def publicKey: String

  /**
   *
   * @param sku the product ID
   * @param activity an activity
   * @return true if the user owns `sku`, false otherwise
   */
  def hasSku(sku: String, activity: Activity): Future[Boolean] = withIAB(activity, iab => {
    iab.hasPurchase(sku).map(has => {
      info(s"User has SKU $sku: $has")
      has
    })
  })

  override def productInfo(sku: String, activity: Activity): Future[ProductInfo] = withIAB(activity, iab => {
    iab.productDetails(sku).map(d => ProductInfo(d.getSku, d.getPrice, d.getTitle, d.getDescription))
  })

  def purchase(sku: String, activity: Activity): Future[String] = withIAB(activity, iab => {
    iab.purchase(activity, sku, 1000).map(_.getSku)
  })

  private def withIAB[T](activity: Activity, f: AsyncIabHelper => Future[T]): Future[T] = {
    val iab = new AsyncIabHelper(activity, new IabHelper(activity, publicKey))
    val result = iab.startSetup.flatMap(_ => f(iab))
    result
      .recover(logException("Unable to sync Google Play purchase status"))
      .onComplete(_ => {
      // finally
      Try(iab.close())
    })
    result
  }

}