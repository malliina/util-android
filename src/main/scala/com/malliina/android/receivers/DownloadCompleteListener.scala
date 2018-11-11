package com.malliina.android.receivers

import android.app.DownloadManager
import android.content.{BroadcastReceiver, Context, Intent}

/** Should this not be registered in androidmanifest.xml?
  */
trait DownloadCompleteListener extends BroadcastReceiver {
  protected val NO_ID = -1L

  def onReceive(context: Context, intent: Intent) {
    intent.getAction match {
      case DownloadManager.ACTION_DOWNLOAD_COMPLETE =>
        val completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, NO_ID)
        if (completedDownloadId != -1L) {
          onDownloadComplete(completedDownloadId)
        }
      case _ => ()
    }
  }

  def onDownloadComplete(id: Long)
}
