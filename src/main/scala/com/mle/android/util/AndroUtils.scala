package com.mle.android.util

import android.content.pm.PackageManager
import android.content.Context
import collection.JavaConversions._

/**
 *
 * @author mle
 */
trait AndroUtils {
  def isPackageinstalled(ctx: Context, packageName: String) =
    ctx.getPackageManager.getInstalledApplications(PackageManager.GET_META_DATA)
      .exists(_.packageName == packageName)
}

object AndroUtils extends AndroUtils