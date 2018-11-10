package com.mle.android.util

import android.util.Log

trait MleLog {

  def tag: String

  def debug(msg: => String): Unit = log(Log.d, msg)

  def info(msg: => String): Unit = log(Log.i, msg)

  def warn(msg: => String, ex: Throwable): Unit =
    log(Log.w, s"$msg. ${failMessage(ex)}")

  def warn(msg: => String): Unit = log(Log.w, msg)

  def err(msg: => String): Unit = log(Log.e, msg)

  private def log(f: (String, String) => Int, msg: => String): Unit = f(tag, msg)

  protected def failMessage(e: Throwable, stackTrace: Boolean = true): String = {
    val exName = e.getClass.getName
    val explanation = Option(e.getMessage).filter(_.trim.nonEmpty).fold("")(msg => s": $msg")
    if (stackTrace) s"$exName$explanation\n${e.getStackTraceString}"
    else s"$exName$explanation"
  }
}

trait UtilLog extends MleLog {
  def tag: String = "com.mle.android"
}
