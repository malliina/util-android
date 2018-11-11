package com.malliina.util

import play.api.libs.json.Json

case class Version(version: String)

object Version {
  implicit val json = Json.format[Version]
}
