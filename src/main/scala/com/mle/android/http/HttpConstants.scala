package com.mle.android.http

/**
 *
 * @author mle
 */
trait HttpConstants {
  val ACCEPT = "Accept"
  val AUTHORIZATION = "Authorization"

  val JSON = "application/json"

  val BAD_REQUEST = 400
  val UNAUTHORIZED = 401
  val NOT_FOUND = 404
  val NOT_ACCEPTABLE = 406
}

object HttpConstants extends HttpConstants
