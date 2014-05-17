package com.mle.android.iap.google

import android.app.Activity
import com.android.iab.util.IabHelper
import scala.util.Try
import scala.concurrent.Future
import com.mle.util.Utils
import Utils.executionContext
import com.mle.android.iap.{ProductInfo, IapUtilsBase}

/**
 *
 * @author mle
 */
trait GoogleIapUtils extends IapUtilsBase {
  def publicKey: String

  /**
   * Android docs suggest we can only call one asynchronous operation on
   * [[com.android.iab.util.IabHelper]] at a time (wtf? per instance?), so
   * others may use this var to see if this class is currently using another instance.
   *
   * TODO: actually fix the concurrency problem instead of this bs.
   *
   * @see [[com.android.iab.util.IabHelper]]
   */
  @volatile var isSyncing = false

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


  def purchase(sku: String, activity: Activity): Future[String] = {
    val iab = new AsyncIabHelper(activity, new IabHelper(activity, publicKey))
    val fut = iab.purchase(activity, sku, 1000).map(_.getSku)
    fut.onComplete(_ => Try(iab.close()))
    fut
  }

  private def withIAB[T](activity: Activity, f: AsyncIabHelper => Future[T]): Future[T] = {
    isSyncing = true
    val iab = new AsyncIabHelper(activity, new IabHelper(activity, publicKey))
    val result = iab.startSetup.flatMap(_ => f(iab))
    //      for {
    //        setupComplete <- iab.startSetup
    //        isSkuPurchased <- iab.hasPurchase(sku)
    //      } yield {
    //        info(s"User has SKU $sku: $isSkuPurchased")
    //        isSkuPurchased
    //      }
    result
      .recover(logException("Unable to sync Google Play purchase status"))
      .onComplete(_ => {
      // finally
      Try(iab.close())
      isSyncing = false
    })
    result
  }

}