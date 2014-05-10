package com.mle.android.network

import android.content.Context
import android.net.wifi.WifiManager
import com.mle.network.NetworkDevice
import android.net.ConnectivityManager

/**
 *
 * @author mle
 */
trait WifiHelpers {
  /**
   *
   * @param ctx context
   * @return the WLAN SSID this device is currently connected to wrapped in an [[Option]], or [[None]] if there is no WiFi connectivity
   */
  def currentSSID(ctx: Context): Option[String] = {
    val wifiManager = ctx.getSystemService(Context.WIFI_SERVICE).asInstanceOf[WifiManager]
    for {
      connInfo <- Option(wifiManager.getConnectionInfo)
      ssid <- Option(connInfo.getSSID)
    } yield ssid
  }

  def network(ip: String) = {
    val lastDotIndex = ip lastIndexOf '.'
    ip.substring(0, lastDotIndex)
  }

  def isWifiConnected(ctx: Context): Boolean = {
    val mgr = ctx.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    Option(mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).exists(_.isConnected)
  }
}

object WifiHelpers extends WifiHelpers
