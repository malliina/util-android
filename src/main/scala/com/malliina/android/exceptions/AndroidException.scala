package com.malliina.android.exceptions

class AndroidException(msg: String, t: Option[Throwable] = None)
  extends Exception(msg, t.orNull)
