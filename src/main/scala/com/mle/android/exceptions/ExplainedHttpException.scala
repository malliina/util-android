package com.mle.android.exceptions

abstract class ExplainedHttpException(content: Option[String], cause: Option[Throwable] = None)
  extends ExplainedException(content getOrElse "", cause) {
  def this(content: Option[String], cause: Throwable) = this(content, Some(cause))

  /**
   * @return an explanation of the problem that we can display to the user
   */
  def reason: String
}