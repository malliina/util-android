package com.mle.json

import play.api.libs.json.{JsResult, JsValue, Format}
import play.api.libs.json.Json._

/**
 * Json reader/writer. Writes toString and reads as specified by `f`.
 *
 * @param reader maps a name to the type
 * @tparam T type of element
 */
class SimpleFormat[T](reader: String => T) extends Format[T] {
  def reads(json: JsValue): JsResult[T] =
    json.validate[String].map(reader)

  def writes(o: T): JsValue = toJson(o.toString)
}