package com.mle.file

import java.io.{FileNotFoundException, File}
import com.mle.storage._
import scala.concurrent.Future
import com.mle.util.Utils
import Utils.executionContext

/**
 *
 * @author mle
 */
trait DiskHelpers {
  /**
   * @param rootDir directory from which to delete files
   * @param maxSize maximum allowed size of directory
   * @param deleteAmount amount of data to delete if the directory size exceeds `maxSize`
   * @return the amount of data deleted
   */
  def maintainDirSize(rootDir: File, maxSize: StorageSize, deleteAmount: StorageSize = 500.megs): Future[StorageSize] =
    storageSizeFuture(rootDir).map(currentSize => {
      if (currentSize > maxSize) {
        //        info(s"App-local music storage size of $currentSize exceeds the cache size of $maxSize, deleting tracks...")
        val deletedSize = free(rootDir, 500.megs)
        //        info(s"Deleted $deletedSize of tracks in an effort to maintain the configured cache size of $maxSize.")
        deleteEmptyDirs(rootDir)
        deletedSize
      } else {
        //        debug(s"App-local music storage consumes $currentSize. The limit is $maxSize. Doing nothing.")
        StorageSize.empty
      }
    })

  def free(dir: File, amount: StorageSize): StorageSize = free(dir, amount.toBytes).bytes

  /**
   * Deletes files under `dir` until `bytes` bytes has been deleted.
   *
   * May delete recursively if required.
   *
   * @param dir root dir from which to delete files
   * @param bytes the amount of data to delete
   * @return the number of bytes deleted
   */
  def free(dir: File, bytes: Long): Long =
    dir.listFiles().foldLeft(0L)((acc, path) => {
      if (acc > bytes) acc
      else {
        if (path.isDirectory) acc + free(path, bytes - acc)
        else {
          val fileSize = path.length()
          if (path.delete()) acc + fileSize
          else acc
        }
      }
    })

  /**
   * Deletes empty directories under `dir`, but not `dir` itself even if it is empty.
   *
   * @param dir the root directory to traverse, deleting empty dirs underneath
   */
  def deleteEmptyDirs(dir: File): Unit = {

    def deleteSubDirs(parent: File): Unit = parent.listFiles().filter(_.isDirectory).foreach(deleteDirs)

    def deleteDirs(subDir: File): Unit = {
      // DFS
      deleteSubDirs(subDir)
      if (subDir.listFiles().length == 0) {
        subDir.delete()
      }
    }

    if (dir.isDirectory) {
      deleteSubDirs(dir)
    }
  }

  def deleteEmptyDirsFuture(dir: File): Future[Unit] = Future(deleteEmptyDirs(dir))

  def dirSize(dir: File): Long =
    if (dir.exists() && dir.isDirectory) {
      dir.listFiles().foldLeft(0L)((acc, file) => {
        if (file.isDirectory) acc + dirSize(file)
        else acc + file.length()
      })
    } else {
      throw new FileNotFoundException(dir.getAbsolutePath)
    }

  def storageSize(dir: File): StorageSize = dirSize(dir).bytes

  def dirSizeFuture(dir: File): Future[Long] = Future(dirSize(dir))

  def storageSizeFuture(dir: File): Future[StorageSize] = dirSizeFuture(dir).map(_.bytes)
}
