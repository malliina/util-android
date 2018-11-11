package com.malliina.android.messaging

import android.content.{ComponentName, Intent, Context}
import android.support.v4.content.WakefulBroadcastReceiver
import android.app.Activity

abstract class GcmReceiver(serviceClass: Class[_]) extends WakefulBroadcastReceiver {
  override def onReceive(context: Context, intent: Intent): Unit = {
    val comp = new ComponentName(context.getPackageName, serviceClass.getName)
    WakefulBroadcastReceiver.startWakefulService(context, intent setComponent comp)
    setResultCode(Activity.RESULT_OK)
  }
}
