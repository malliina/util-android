package com.malliina.android.iap.google

import java.io.Closeable
import java.util.UUID

import android.app.Activity
import com.android.iab.util.IabHelper.{OnIabPurchaseFinishedListener, OnIabSetupFinishedListener, QueryInventoryFinishedListener}
import com.android.iab.util._
import com.malliina.android.iap.IapException
import com.malliina.concurrent.ExecutionContexts.cached
import com.malliina.util.Utils

import scala.collection.JavaConversions._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/** Converts Google Play's callback-based IAB API to one based on [[scala.concurrent.Future]]s.
  *
  * The UI thread is used to submit the asynchronous requests; exceptions are apparently thrown otherwise.
  */
class AsyncIabHelper(activity: Activity, val iabHelper: IabHelper) extends Closeable {
  iabHelper enableDebugLogging true

  // not sure why I need to specify the types

  // This future must complete before we run any IAB queries, therefore this class wraps calls in `ensureSetupComplete`
  val startSetup: Future[IabResult] =
    makeFuture[IabResult, SetupListener](new SetupListener)(iabHelper.startSetup)

  /**
    * @param sku the product ID
    * @return true if the currently signed in user owns SKU `sku`, false otherwise
    */
  def hasPurchase(sku: String): Future[Boolean] = ensureSetupComplete {
    inventoryFuture(iabHelper.queryInventoryAsync).map(_.hasPurchase(sku))
  }

  /** Queries for details of the items with the SKUs given in `skus`.
    *
    * Use this to query available (and possibly unowned) items: check the SKUs in
    * advance from the Google Developer Console.
    *
    * @param skus            the SKUs to query for
    * @param querySkuDetails whether to return SKU details ("should be set to `true`")
    * @return details of `skus`
    */
  def inventory(skus: Seq[String], querySkuDetails: Boolean = true): Future[Inventory] = ensureSetupComplete {
    inventoryFuture(listener => iabHelper.queryInventoryAsync(querySkuDetails, skus, listener))
  }

  def productDetails(sku: String): Future[SkuDetails] = inventory(Seq(sku)).map(_.getSkuDetails(sku))

  /** Purchases `sku`.
    *
    * The string `payload` will be returned in subsequent queries about this purchase.
    *
    * The returned future only completes successfully if the IabResult is successful and the developer payload of the
    * returned `Purchase` matches `payload`. If the payloads don't match, the future fails with a
    * [[com.malliina.android.iap.google.PayloadMismatchException]].
    *
    * @param activity    your activity
    * @param sku         SKU of item to purchase
    * @param requestCode any positive integer; will be returned in onActivityResult with the purchase response
    * @param payload     supplemental information about the order; can be an empty string
    * @return the purchase
    * @see http://developer.android.com/training/in-app-billing/purchase-iab-products.html
    */
  def purchase(activity: Activity, sku: String, requestCode: Int, payload: String): Future[Purchase] =
    ensureSetupComplete {
      makeFuture[Purchase, PurchaseListener](new PayloadVerifyingPurchaseListener(payload))(listener => {
        iabHelper.launchPurchaseFlow(activity, sku, requestCode, listener, payload)
      })
    }

  def purchase(activity: Activity, sku: String, requestCode: Int): Future[Purchase] =
    purchase(activity, sku, requestCode, UUID.randomUUID().toString)

  def close(): Unit = iabHelper.dispose()

  private def makeFuture[T, L <: FutureBuilder[T]](listener: L)(f: L => Unit): Future[T] = {
    val l = listener
    withGooglePlay {
      // submits the request on the UI thread, apparently required
      activity.runOnUiThread(Utils.runnable(f(l)))
      l.future
    }
  }

  private def ensureSetupComplete[T](f: => Future[T]): Future[T] = startSetup flatMap (_ => f)

  private def inventoryFuture(f: InventoryListener => Unit): Future[Inventory] =
    makeFuture[Inventory, InventoryListener](new InventoryListener)(f)

  /**
    * Operations on [[com.android.iab.util.IabHelper]] may throw [[java.lang.NullPointerException]] if the device does
    * not have Google Play installed. (For example, when the app runs on the emulator.) This method executes `f` and
    * should any exception be thrown, fails the resulting future.
    *
    * @param f IAB code to run
    * @tparam T desired result type, for example [[com.android.iab.util.Purchase]]
    * @return the future result
    */
  private def withGooglePlay[T](f: => Future[T]): Future[T] = Try(f) match {
    case Success(fut) => fut
    case Failure(t) => Future.failed[T](new GooglePlayException("Google Play error. Ensure that Google Play is installed on the device.", t))
  }

  class SetupListener extends OnIabSetupFinishedListener with FutureBuilder[IabResult] {
    def onIabSetupFinished(result: IabResult): Unit = handle(result, result)
  }

  class InventoryListener extends QueryInventoryFinishedListener with FutureBuilder[Inventory] {
    def onQueryInventoryFinished(result: IabResult, inv: Inventory): Unit = handle(result, inv)
  }

  class PurchaseListener extends OnIabPurchaseFinishedListener with FutureBuilder[Purchase] {
    def onIabPurchaseFinished(result: IabResult, info: Purchase): Unit = handle(result, info)
  }

  class PayloadVerifyingPurchaseListener(expectedPayload: String) extends PurchaseListener {
    override def onIabPurchaseFinished(result: IabResult, info: Purchase): Unit = {
      if (result.isFailure || info.getDeveloperPayload == expectedPayload)
        super.onIabPurchaseFinished(result, info)
      else
        p failure new PayloadMismatchException(s"Payload of ${info.getDeveloperPayload} does not match expected payload of $expectedPayload", result, info)
    }
  }

  trait FutureBuilder[T] {
    val p = Promise[T]()

    def handle(result: IabResult, item: T): Unit =
      if (result.isSuccess) p trySuccess item
      else p tryFailure new IabResultException(result.getMessage, result)

    def future = p.future
  }

}

class GooglePlayException(msg: String, val throwable: Throwable) extends IapException(msg, Some(throwable))

class IabResultException(msg: String, val result: IabResult) extends IapException(msg)

class PayloadMismatchException(msg: String, val result: IabResult, val purchase: Purchase) extends IapException(msg)
