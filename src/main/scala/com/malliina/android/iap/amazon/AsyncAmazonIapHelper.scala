package com.malliina.android.iap.amazon

import android.content.Context
import com.amazon.inapp.purchasing.ItemDataResponse.ItemDataRequestStatus
import com.amazon.inapp.purchasing._
import com.malliina.concurrent.ExecutionContexts.cached
import com.malliina.android.iap._
import com.malliina.concurrent.PromiseHelpers

import scala.concurrent.Promise

/** This class registers itself as the observer for the [[com.amazon.inapp.purchasing.PurchasingManager]] upon
  * construction.
  *
  * Clients shall not use this class directly. Instead, use [[com.malliina.android.iap.amazon.AmazonIapUtils]].
  *
  * TODO: Create rx observables that get new values when the callbacks are called. Futures are one-off and a bad match
  * for this API.
  */
class AsyncAmazonIapHelper(ctx: Context) extends AmazonPurchasingObserver(ctx) with PromiseHelpers {
  private val isSandboxAllowed: Boolean = true
  private val purchaseUpdateFailureMessage = "Unable to read purchase status."
  private val itemStatusFailureMessage = "Unable to read item data."
  private val getUserFailureMessage = "Unable to read the current user ID."

  private val isSandboxPromise = Promise[Boolean]()
  private val userIdPromise = Promise[String]()
  private val entitledPromise = Promise[Set[String]]()
  private val revokedPromise = Promise[Set[String]]()
  private val purchasePromise = Promise[String]()
  private val availableItemsPromise = Promise[Set[ProductInfo]]()

  val isSandbox = isSandboxPromise.future
  val entitledSkus = entitledPromise.future
  val revokedSkus = revokedPromise.future
  val availableItems = availableItemsPromise.future
  val userId = userIdPromise.future
  val purchase = purchasePromise.future

  // I think there's at most one observer at any given time and the last one registered wins
  PurchasingManager.registerObserver(this)

  def hasSku(sku: String) = entitledSkus.map(_.contains(sku))

  def onEntitledSkus(skus: Set[String]): Unit = trySuccess(skus, entitledPromise)

  def onRevokedSkus(skus: Set[String]): Unit = trySuccess(skus, revokedPromise)

  override def onSdkAvailable(isSandboxMode: Boolean): Unit = {
    super.onSdkAvailable(isSandboxMode)
    trySuccess(isSandboxMode, isSandboxPromise)
    if (isSandboxMode && !isSandboxAllowed) {
      tryFailure(new SandboxModeException, userIdPromise)
    }
    if (!isSandboxMode || isSandboxAllowed) {
      // the getUserId callback calls the get purchase status,
      // the callback of which will eventually complete the promises
      PurchasingManager.initiateGetUserIdRequest()
    }
  }

  def onUserId(userId: String): Unit = {
    //    info(s"Amazon IAP user identified as: $userId")
    trySuccess(userId, userIdPromise)
  }

  override def onGetUserIdFailed(response: GetUserIdResponse): Unit = {
    super.onGetUserIdFailed(response)
    val ex = new IapException(getUserFailureMessage)
    tryFailure(ex, userIdPromise, entitledPromise, revokedPromise)
  }

  override def onPurchaseStatusFailed(response: PurchaseUpdatesResponse): Unit = {
    super.onPurchaseStatusFailed(response)
    val ex = new IapException(purchaseUpdateFailureMessage)
    tryFailure(ex, entitledPromise, revokedPromise)
  }


  override def onItemDataResponse(response: ItemDataResponse): Unit = {
    super.onItemDataResponse(response)
    if (response.getItemDataRequestStatus == ItemDataRequestStatus.SUCCESSFUL) {
      import scala.collection.JavaConversions._
      val infos = response.getItemData.values()
        .map(item => ProductInfo(item.getSku, item.getPrice, item.getTitle, item.getDescription)).toSet
      trySuccess(infos, availableItemsPromise)
    } else {
      val ex = new IapException(itemStatusFailureMessage)
      tryFailure(ex, availableItemsPromise)
    }
  }

  def onPurchaseSucceeded(sku: String): Unit = trySuccess(sku, purchasePromise)

  def onPurchaseFailed(response: PurchaseResponse): Unit = {
    import com.amazon.inapp.purchasing.PurchaseResponse.PurchaseRequestStatus._
    val ex = response.getPurchaseRequestStatus match {
      case INVALID_SKU => new AmazonInvalidSkuException(response)
      case ALREADY_ENTITLED => new AmazonAlreadyPurchasedException(response)
      case _ => new AmazonPurchaseException(IapUtilsBase.purchaseFailedMessage, response)
    }
    tryFailure(ex, purchasePromise)
  }
}

class AmazonPurchaseException(msg: String, val response: PurchaseResponse) extends IapException(msg)

class AmazonInvalidSkuException(val response: PurchaseResponse) extends InvalidSkuException

class AmazonAlreadyPurchasedException(response: PurchaseResponse) extends AlreadyPurchasedException

class SandboxModeException extends IapException("The purchasing manager is running in sandbox mode. Please ensure that you have downloaded this app from Amazon AppStore, then try again.")
