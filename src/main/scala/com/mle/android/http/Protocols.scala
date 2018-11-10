package com.mle.android.http

object Protocols extends Enumeration {
  type Protocol = Value
  val Http, Https = Value


  def withNameIgnoreCase(name: String) = name.toLowerCase match {
    case "http" => Http
    case "https" => Https
    case other => throw new NoSuchElementException(s"Unknown Protocols name: $other")
  }
}
