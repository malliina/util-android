package com.mle.android.http


/**
 *
 * @author mle
 */
class AuthHttpClient(username: String, password: String) extends JsonHttpClient {
  addHeaders(
    HttpConstants.AUTHORIZATION -> HttpUtil.authorizationValue(username, password)
  )
}
