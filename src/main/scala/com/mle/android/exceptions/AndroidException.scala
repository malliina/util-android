package com.mle.android.exceptions

class AndroidException(msg: String, t: Option[Throwable] = None) extends Exception(msg, t getOrElse null)