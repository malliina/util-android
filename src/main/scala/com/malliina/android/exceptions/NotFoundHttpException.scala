package com.malliina.android.exceptions

class NotFoundHttpException(cause: Throwable)
  extends ExplainedHttpException(None, cause = cause) {

  override def reason: String = "Not found"
}
