package com.malliina.android.http

import android.util.Base64

object HttpUtil {
  def authorizationValue(username: String, password: String) =
    "Basic " + Base64.encodeToString((username + ":" + password).getBytes("UTF-8"), Base64.DEFAULT)
}
