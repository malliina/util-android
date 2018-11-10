package com.mle.android.iap.amazon

import com.amazon.inapp.purchasing._
import android.content.Context
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse.PurchaseUpdatesRequestStatus
import collection.JavaConversions._
import com.amazon.inapp.purchasing.Item.ItemType
import com.amazon.inapp.purchasing.PurchaseResponse.PurchaseRequestStatus
import com.mle.android.util.UtilLog

abstract class AmazonPurchasingObserver(ctx: Context) extends BasePurchasingObserver(ctx) with UtilLog {
  def onUserId(userId: String)

  def onEntitledSkus(skus: Set[String])

  def onRevokedSkus(skus: Set[String])

  def onPurchaseSucceeded(sku: String)

  def onPurchaseFailed(response: PurchaseResponse)

  def onGetUserIdFailed(response: GetUserIdResponse) = {
    warn("Get user ID request failed")
  }

  def onPurchaseStatusFailed(response: PurchaseUpdatesResponse) = {
    warn("Unable to read purchase status")
  }

  /**
   * Callback for PurchasingManager.registerObserver(Activity)
   *
   * @param isSandboxMode
   */
  override def onSdkAvailable(isSandboxMode: Boolean): Unit = {
    info(s"SDK is in sandbox mode: $isSandboxMode")
  }

  /**
   * Callback for PurchasingManager.initiateGetUserIdRequest()
   *
   * @param response
   */
  override def onGetUserIdResponse(response: GetUserIdResponse): Unit = {
    if (response.getUserIdRequestStatus == GetUserIdResponse.GetUserIdRequestStatus.SUCCESSFUL) {
      onUserId(response.getUserId)
    } else {
      onGetUserIdFailed(response)
    }
  }

  /**
   * Callback for initiatePurchaseUpdatesRequest(Offset).
   *
   * "You must use this method to sync purchases made from other devices onto this
   * device, and to sync revoked entitlements across all instances of your app."
   *
   * @param response
   */
  override def onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse): Unit = {
    response.getPurchaseUpdatesRequestStatus match {
      case PurchaseUpdatesRequestStatus.SUCCESSFUL =>
        onRevokedSkus(response.getRevokedSkus.toSet)
        val entitlementReceipts = response.getReceipts.filter(_.getItemType == ItemType.ENTITLED)
        onEntitledSkus(entitlementReceipts.map(_.getSku).toSet)
      case PurchaseUpdatesRequestStatus.FAILED =>
        onPurchaseStatusFailed(response)
    }
  }

  override def onPurchaseResponse(response: PurchaseResponse): Unit =
    if (response.getPurchaseRequestStatus == PurchaseRequestStatus.SUCCESSFUL)
      onPurchaseSucceeded(response.getReceipt.getSku)
    else
      onPurchaseFailed(response)


  override def onItemDataResponse(response: ItemDataResponse): Unit = ()
}
