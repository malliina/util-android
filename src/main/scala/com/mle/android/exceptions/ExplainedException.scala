package com.mle.android.exceptions

/**
 * An exception with a message that the user understands;
 * assume it will be displayed to the user as feedback.
 *
 * @param msg the exception message
 * @param t the cause
 */
class ExplainedException(msg: String, t: Option[Throwable] = None)
  extends Exception(msg, t getOrElse null)