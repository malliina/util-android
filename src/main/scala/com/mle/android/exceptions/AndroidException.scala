package com.mle.android.exceptions

/**
 *
 * @author mle
 */
class AndroidException(msg: String, t: Option[Throwable] = None) extends Exception(msg, t getOrElse null)