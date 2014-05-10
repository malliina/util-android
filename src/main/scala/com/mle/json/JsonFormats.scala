package com.mle.json

import play.api.libs.json._
import play.api.libs.json.Json._
import scala.concurrent.duration._
import com.mle.storage.StorageSize
import com.mle.storage.StorageLong

/**
 *
 * @author mle
 */
trait JsonFormats {

  /**
   * Serializes Duration to Long, deserializes Double to Duration.
   *
   * One second granularity.
   */
  implicit object duration extends Format[Duration] {
    def reads(json: JsValue): JsResult[Duration] =
      json.validate[Double].map(_.seconds)

    def writes(o: Duration): JsValue = toJson(o.toSeconds)
  }

  implicit object storageSize extends Format[StorageSize] {
    override def reads(json: JsValue): JsResult[StorageSize] =
      json.validate[Long].map(_.bytes)

    override def writes(o: StorageSize): JsValue =
      Json.toJson(o.toBytes)
  }

  def constant(constant: String) = new Reads[String] {
    def reads(json: JsValue): JsResult[String] = JsSuccess(constant)
  }
}

object JsonFormats extends JsonFormats