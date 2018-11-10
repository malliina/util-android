package com.mle.android.exceptions

import cz.msebera.android.httpclient.client.HttpResponseException

class UnauthorizedHttpException(cause: HttpResponseException)
  extends ExplainedHttpException(None, cause) {
  override val reason = "Unauthorized. Check your credentials."
}
