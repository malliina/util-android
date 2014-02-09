package com.mle.android.iap.amazon

import android.content.Context
import com.amazon.inapp.purchasing.{PurchasingManager, GetUserIdResponse, PurchaseUpdatesResponse, PurchaseResponse}
import scala.concurrent.{Promise, promise}
import com.mle.android.exceptions.AndroidException
import com.mle.util.Utils
import Utils.executionContext
import scala.util.Try

/**
 * This class registers itself as the observer for the
 * [[com.amazon.inapp.purchasing.PurchasingManager]] upon
 * construction.
 *
 * Clients shall not use this class directly. Instead, use
 * [[com.mle.android.iap.amazon.AmazonIapUtils]].
 *
 * TODO: Create rx observables that get new values when
 * the callbacks are called. Futures are one-off and a bad
 * match for this API.
 *
 * @author mle
 */
class AsyncAmazonIapHelper(ctx: Context) extends AmazonPurchasingObserver(ctx) {
  private val isSandboxAllowed: Boolean = true
  private val purchaseUpdateFailureMessage = "Unable to read purchase status."
  private val getUserFailureMessage = "Unable to read the current user ID."
  private val purchaseFailedMessage = "The purchase did not complete successfully."

  private val isSandboxPromise = promise[Boolean]()
  private val userIdPromise = promise[String]()
  private val entitledPromise = promise[Set[String]]()
  private val revokedPromise = promise[Set[String]]()
  private val purchasePromise = promise[String]()

  val isSandbox = isSandboxPromise.future
  val entitledSkus = entitledPromise.future
  val revokedSkus = revokedPromise.future
  val userId = userIdPromise.future
  val purchase = purchasePromise.future

  // I think there's at most one observer at any given time and the last one registered wins
  PurchasingManager.registerObserver(this)

  def hasSku(sku: String) = entitledSkus.map(_.contains(sku))

  def onEntitledSkus(skus: Set[String]): Unit = completeIfPossible(skus, entitledPromise)

  def onRevokedSkus(skus: Set[String]): Unit = completeIfPossible(skus, revokedPromise)

  override def onSdkAvailable(isSandboxMode: Boolean): Unit = {
    super.onSdkAvailable(isSandboxMode)
    completeIfPossible(isSandboxMode, isSandboxPromise)
    if (isSandboxMode && !isSandboxAllowed) {
      failIfPossible(new SandboxModeException, userIdPromise)
    }
    if (!isSandboxMode || isSandboxAllowed) {
      // the getUserId callback calls the get purchase status,
      // the callback of which will eventually complete the promises
      PurchasingManager.initiateGetUserIdRequest()
    }
  }

  def onUserId(userId: String) = {
    info(s"Amazon IAP user identified as: $userId")
    completeIfPossible(userId, userIdPromise)
  }

  override def onGetUserIdFailed(response: GetUserIdResponse): Unit = {
    super.onGetUserIdFailed(response)
    val ex = new AmazonIapException(getUserFailureMessage)
    failIfPossible(ex, userIdPromise, entitledPromise, revokedPromise)
  }

  override def onPurchaseStatusFailed(response: PurchaseUpdatesResponse): Unit = {
    super.onPurchaseStatusFailed(response)
    val ex = new AmazonIapException(purchaseUpdateFailureMessage)
    failIfPossible(ex, entitledPromise, revokedPromise)
  }

  def onPurchaseSucceeded(sku: String): Unit = completeIfPossible(sku, purchasePromise)

  def onPurchaseFailed(response: PurchaseResponse): Unit = {
    import com.amazon.inapp.purchasing.PurchaseResponse.PurchaseRequestStatus._
    val ex = response.getPurchaseRequestStatus match {
      case INVALID_SKU => new InvalidSkuException(response)
      case ALREADY_ENTITLED => new AlreadyPurchased(response)
      case _ => new AmazonPurchaseException(purchaseFailedMessage, response)
    }
    failIfPossible(ex, purchasePromise)
  }

  // Defensive coding because the IAP API is not thread-safe as far as I know

  private def completeIfPossible[T](value: T, promise: Promise[T]): Unit =
    tryIfPossible(promise)(_ trySuccess value)

  private def failIfPossible(t: Throwable, promises: Promise[_]*): Unit =
    promises foreach (p => tryIfPossible(p)(_ tryFailure t))

  private def tryIfPossible[T, U](promise: Promise[T])(f: Promise[T] => U): Option[Try[U]] = {
    if (!promise.isCompleted) {
      Some(Try(f(promise)))
    } else {
      None
    }
  }
}

class AmazonIapException(msg: String) extends AndroidException(msg)

class AmazonPurchaseException(msg: String, val response: PurchaseResponse) extends AmazonIapException(msg)

class InvalidSkuException(response: PurchaseResponse) extends AmazonPurchaseException("The product ID did not exist. Try again later.", response)

class AlreadyPurchased(response: PurchaseResponse) extends AmazonPurchaseException("The product has already been purchased.", response)

class SandboxModeException extends AmazonIapException("The purchasing manager is running in sandbox mode. Please ensure that you have downloaded this app from Amazon AppStore, then try again.")
