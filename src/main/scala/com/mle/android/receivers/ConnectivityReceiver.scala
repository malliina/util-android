package com.mle.android.receivers

import android.content.{Intent, Context, BroadcastReceiver}
import android.net.{NetworkInfo, ConnectivityManager}
import com.mle.android.util.UtilLog

trait ConnectivityReceiver extends BroadcastReceiver with UtilLog {

  import ConnectivityReceiver._

  def onWifiConnected(ctx: Context, activeNetwork: NetworkInfo): Unit

  override def onReceive(ctx: Context, intent: Intent): Unit = {
    if (intent.getAction == ConnectivityManager.CONNECTIVITY_ACTION) {
      val connMgr = ctx.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
      Option(connMgr.getActiveNetworkInfo)
        .filter(network => network.getType == ConnectivityManager.TYPE_WIFI && network.isConnected)
        .foreach(info => onWifiConnectedEvent(ctx, info))
    }
  }

  protected def onWifiConnectedEvent(ctx: Context, activeNetwork: NetworkInfo): Unit = {
    //    describe(activeNetwork)
    // only sync every second intent due to duplicate events...
    shouldHandleEvent = !shouldHandleEvent
    if (shouldHandleEvent) {
      onWifiConnected(ctx, activeNetwork)
    }
  }
}

object ConnectivityReceiver {
  // The "wifi connected" event is received twice every time, with seemingly identical NetworkInfo objects. Therefore
  // we only act upon every second intent. This flag is toggled and used to determine whether to act or not.
  private var shouldHandleEvent: Boolean = false
}
