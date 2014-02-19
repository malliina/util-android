package com.mle.android.exceptions


/**
 *
 * @author mle
 */
class NotFoundHttpException(cause: Throwable)
  extends ExplainedHttpException(None, cause = cause) {

  override def reason: String = "Not found"
}