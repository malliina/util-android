package com.mle.android.exceptions

import org.apache.http.client.HttpResponseException

/**
 *
 * @author mle
 */
class UnauthorizedHttpException(cause: HttpResponseException)
  extends ExplainedHttpException(None, cause) {
  override val reason = "Unauthorized. Check your credentials."
}
