package com.malliina.android.ui

import java.util.concurrent.ScheduledFuture

import android.app.{Activity, DownloadManager}
import android.content.Context
import android.app.DownloadManager._
import android.database.Cursor
import android.net.Uri
import com.malliina.concurrent.Scheduling

import concurrent.duration._

trait DownloadHelper {
  private var poller: Option[ScheduledFuture[_]] = None

  def downloadsAbsolutePathPrefix: String

  def musicBaseDirLength: Int

  def activity: Activity

  /**
   * Called when the progress of still ongoing downloads has been updated.
   *
   * Note that this method is not called when a download is completed.
   *
   * @param downloads all downloads with status STATUS_RUNNING
   */
  def onDownloadProgressUpdate(downloads: Seq[DownloadStatus]): Unit = ()

  def downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE).asInstanceOf[DownloadManager]

  def startPollDownloadProgress() {
    stopPollDownloadProgress()
    val task = Scheduling.every(1000.milliseconds) {
      try {
        //        debug("Querying download progress...")
        val downloadStatuses = runningDownloads()
        onDownloadProgressUpdate(downloadStatuses)
      } catch {
        case e: Exception =>
        //          warn(s"Downloads query failed", e)
      }
    }
    poller = Some(task)
  }

  def stopPollDownloadProgress() {
    poller.foreach(_.cancel(true))
    poller = None
  }

  def runningDownloads(): Seq[DownloadStatus] = {
    val query = new DownloadManager.Query()
      .setFilterByStatus(STATUS_RUNNING)
    // STATUS_FAILED STATUS_SUCCESSFUL STATUS_PAUSED STATUS_PENDING
    // query might return null
    val cursorOpt: Option[Cursor] = Option(downloadManager.query(query))
    cursorOpt.fold(Seq.empty[DownloadStatus])(c => {
      try {
        map(c, readStatus).flatten
      } finally {
        c.close()
      }
    })
  }

  /**
   *
   * @param cursor
   * @return the status of a download that has previously started wrapped in an Option, or None if it's not started yet
   */
  def readStatus(cursor: Cursor): Option[DownloadStatus] = {
    def column[T](f: Cursor => Int => T, columnName: String) = f(cursor)(cursor getColumnIndex columnName)
    def longColumn(columnName: String) = column(_.getLong, columnName)
    def stringColumn(columnName: String) = column(_.getString, columnName)
    def intColumn(columnName: String) = column(_.getInt, columnName)

    val status = intColumn(COLUMN_STATUS)
    val reason = intColumn(COLUMN_REASON)

    def relativeDestPath =
      Option(stringColumn(COLUMN_LOCAL_URI)).map(Uri.parse).map(_.getPath)
        .filter(_.startsWith(downloadsAbsolutePathPrefix))
        .map(_.drop(musicBaseDirLength))

    relativeDestPath.map(path => DownloadStatus(
      longColumn(COLUMN_ID),
      stringColumn(COLUMN_TITLE),
      stringColumn(COLUMN_DESCRIPTION),
      path,
      longColumn(COLUMN_BYTES_DOWNLOADED_SO_FAR),
      longColumn(COLUMN_TOTAL_SIZE_BYTES),
      describeStatus(status),
      describeReason(status, reason)
    ))
  }

  def queryStatus(id: Long): Option[DownloadStatus] = {
    val query = new DownloadManager.Query().setFilterById(id)
    Option(downloadManager query query).flatMap(cursor => {
      try {
        if (cursor.moveToFirst()) {
          readStatus(cursor)
        } else {
          None
        }
      } finally {
        cursor.close()
      }
    })
  }

  /**
   * Converts a cursor to a sequence of items.
   *
   * @param cursor the cursor
   * @param f function that converts one cursor row to an item
   * @tparam T type of item
   * @return a sequence of items
   */
  def map[T](cursor: Cursor, f: Cursor => T): Seq[T] = {
    def mapAcc(acc: Seq[T]): Seq[T] =
      if (cursor.moveToNext()) mapAcc(acc :+ f(cursor))
      else acc
    if (cursor.moveToFirst()) mapAcc(Seq(f(cursor)))
    else Seq.empty[T]
  }

  def describeStatus(status: Int): String = status match {
    case STATUS_FAILED => "Failed"
    case STATUS_PAUSED => "Paused"
    case STATUS_PENDING => "Pending"
    case STATUS_RUNNING => "Downloading"
    case STATUS_SUCCESSFUL => "Complete"
    case _ => "Unknown"
  }

  def describeReason(status: Int, reason: Int): String = {
    status match {
      case STATUS_FAILED =>
        reason match {
          case ERROR_CANNOT_RESUME => "Cannot resume"
          case ERROR_DEVICE_NOT_FOUND => "Device not found"
          case ERROR_FILE_ALREADY_EXISTS => "File already exists"
          case ERROR_FILE_ERROR => "File error"
          case ERROR_HTTP_DATA_ERROR => "HTTP data error"
          case ERROR_INSUFFICIENT_SPACE => "Insufficient space"
          case ERROR_TOO_MANY_REDIRECTS => "Too many redirects"
          case ERROR_UNHANDLED_HTTP_CODE => "Unhandled HTTP code"
          case _ => "Unknown"
        }
      case STATUS_PAUSED =>
        reason match {
          case PAUSED_QUEUED_FOR_WIFI => "Waiting for WiFi"
          case PAUSED_WAITING_FOR_NETWORK => "Waiting for network"
          case PAUSED_WAITING_TO_RETRY => "Waiting to retry"
          case _ => "Unknown"
        }
      case _ => "Unknown"
    }
  }
}

case class DownloadStatus(id: Long,
                          title: String,
                          description: String,
                          localPath: String,
                          bytesDownloaded: Long,
                          totalSizeBytes: Long,
                          status: String,
                          reason: String)