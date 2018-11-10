package com.mle.android.http

import android.content.Context
import com.loopj.android.http.AsyncHttpResponseHandler
import com.mle.android.exceptions.AndroidException
import com.mle.android.util.UtilLog
import com.mle.concurrent.ExecutionContexts.cached
import com.mle.concurrent.Futures
import cz.msebera.android.httpclient.entity.StringEntity
import play.api.libs.json.{JsResultException, JsValue, Json, Reads}

import scala.concurrent.{Future, Promise}

trait JsonHttpClient extends FutureHttpClient with UtilLog {
  /** HTTP GETs `uri` and maps the JSON in the response content to type `T`.
    *
    * The returned [[Future]] fails with a [[JsResultException]] if the non-empty response content fails to map to `T`.
    * Empty responses fail with a [[AndroidException]]. May also fail with any exception `get` may fail with.
    *
    * @param uri uri to GET
    * @tparam T type of response
    * @return an instance of T
    */
  def getJson[T: Reads](uri: String): Future[T] =
    get(uri).map(response => {
      response.content.map(c => {
        try {
          Json.parse(c).as[T]
        } catch {
          case jre: JsResultException =>
            warn(s"Response content does not conform to expected JSON format: $c", jre)
            throw jre
        }
      }).getOrElse(throw new AndroidException("HTTP response content is empty, expected JSON."))
    })

  def post(ctx: Context, uri: String, json: JsValue): Future[HttpResponse] =
    post(ctx, uri, json, textResponseHandler)

  def post[T](ctx: Context, uri: String, json: JsValue, f: Promise[T] => AsyncHttpResponseHandler): Future[T] =
    Futures.promisedFuture[T] { p =>
      val entity = new StringEntity(Json stringify json)
      httpClient.post(ctx, transformUri(uri), entity, HttpConstants.JSON, f(p))
    }
}
