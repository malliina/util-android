package com.mle.android.http

import scala.concurrent.duration.DurationInt

/** HTTP client that assumes the same endpoint is used for all requests.
  *
  * Therefore modifies the methods so that only the resource, not the
  * absolute URI, must be passed in when making requests.
  */
class BasicHttpClient(endpoint: IEndpoint)
  extends AuthHttpClient(endpoint.username, endpoint.password) {
  httpClient setTimeout 5.seconds.toMillis.toInt

  private def scheme = if (endpoint.protocol == Protocols.Https) "https" else "http"

  val baseUrl = s"$scheme://${endpoint.host}:${endpoint.port}"

  override def transformUri(uri: String): String = baseUrl + uri
}
