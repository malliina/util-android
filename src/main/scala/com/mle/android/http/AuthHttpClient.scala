package com.mle.android.http

class AuthHttpClient(username: String, password: String) extends JsonHttpClient {
  addHeaders(
    HttpConstants.AUTHORIZATION -> HttpUtil.authorizationValue(username, password)
  )
}
