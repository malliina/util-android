package com.mle.json

import play.api.libs.json._
import play.api.libs.json.Json._
import scala.concurrent.duration._

/**
 *
 * @author mle
 */
trait JsonHelpers {
  /**
   * Serializes Duration to Long, deserializes Double to Duration.
   *
   * One second granularity.
   */
  implicit object durationFormat extends Format[Duration] {
    def writes(o: Duration): JsValue = toJson(o.toSeconds)

    def reads(json: JsValue): JsResult[Duration] =
      json.validate[Double].map(_.seconds)
  }

  def constant(constant: String) = new Reads[String] {
    def reads(json: JsValue): JsResult[String] = JsSuccess(constant)
  }
}