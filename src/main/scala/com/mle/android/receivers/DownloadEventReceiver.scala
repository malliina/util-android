package com.mle.android.receivers

import android.app.DownloadManager
import android.content.{BroadcastReceiver, Context, Intent}
import com.mle.android.util.UtilLog

/** TODO remove this nonsense.
  */
class DownloadEventReceiver extends BroadcastReceiver with UtilLog {

  val NO_ID = -1L

  def downloadManager(ctx: Context) = ctx.getSystemService(Context.DOWNLOAD_SERVICE).asInstanceOf[DownloadManager]

  def onReceive(context: Context, intent: Intent) {
    intent.getAction match {
      case DownloadManager.ACTION_DOWNLOAD_COMPLETE =>
        val completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, NO_ID)
        if (completedDownloadId != -1L) {
          //          info(s"Download with id: $completedDownloadId is complete.")
        }
      case DownloadManager.ACTION_NOTIFICATION_CLICKED =>
        DownloadEventReceiver.openDownloadsActivity(context)
      case _ => ()
    }
  }
}

object DownloadEventReceiver {
  /**
    * Opens the Downloads app.
    */
  def openDownloadsActivity(context: Context) {
    val intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  }
}
