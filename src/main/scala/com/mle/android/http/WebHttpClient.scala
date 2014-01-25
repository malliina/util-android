package com.mle.android.http

import com.loopj.android.http.{RequestParams, AsyncHttpResponseHandler, AsyncHttpClient}
import concurrent.duration._
import java.io.File
import org.apache.http.entity.StringEntity
import play.api.libs.json.{JsResultException, JsValue, Reads, Json}
import com.mle.util.Utils
import Utils.executionContext
import scala.concurrent.{Promise, Future, promise}
import com.mle.android.exceptions.AndroidException
import com.mle.android.util.UtilLog
import android.content.Context

/**
 * Wraps AsyncHttpClient in order to provide a Future-based HTTP API instead of a callback-based one.
 *
 * @author mle
 */
class WebHttpClient(host: String, port: Int, username: String, password: String, protocol: Protocols.Protocol) extends UtilLog {
  def this(endpoint: IEndpoint) = this(endpoint.host, endpoint.port, endpoint.username, endpoint.password, endpoint.protocol)

  private def scheme = if (protocol == Protocols.Https) "https" else "http"

  val baseUrl = s"$scheme://$host:$port"

  val httpClient = new AsyncHttpClient
  httpClient setSSLSocketFactory MySslSocketFactory.allowAllCertificatesSocketFactory()
  httpClient setTimeout (5 seconds).toMillis.toInt

  addHeaders(
    HttpConstants.AUTHORIZATION -> HttpUtil.authorizationValue(username, password)
  )

  def addHeaders(headers: (String, String)*) = headers.foreach {
    case (key, value) => httpClient.addHeader(key, value)
  }

  def getEmpty(resource: String) = get(resource).map(_ => ())

  def get(resource: String): Future[HttpResponse] = get(resource, buildResponseHandler)

  def get[T](resource: String, f: Promise[T] => AsyncHttpResponseHandler): Future[T] =
    promisedFuture[T](p => httpClient.get(uri(resource), f(p)))

  def post(ctx: Context, resource: String, json: JsValue): Future[HttpResponse] =
    post(ctx, resource, json, buildResponseHandler)

  def post[T](ctx: Context, resource: String, json: JsValue, f: Promise[T] => AsyncHttpResponseHandler): Future[T] =
    promisedFuture[T](p => {
      val entity = new StringEntity(Json stringify json)
      httpClient.post(ctx, uri(resource), entity, HttpConstants.JSON, f(p))
    })

  def postFileEmpty(resource: String, file: File): Future[Unit] =
    postFileEmpty(resource, file).map(_ => ())

  def postFile(resource: String, file: File): Future[HttpResponse] =
    postFile(resource, file, buildResponseHandler)

  /**
   * Performs a multipart/form-data upload of `file`.
   *
   * @param file file to upload
   */
  def postFile[T](resource: String, file: File, f: Promise[T] => AsyncHttpResponseHandler): Future[T] =
    promisedFuture[T](p => {
      val params = new RequestParams()
      params.put("track", file)
      val destUri = uri(resource)
      httpClient.post(destUri, params, f(p))
    })

  /**
   * Constructs a future that is completed according to `keepPromise`. This pattern
   * can be used to convert callback-based APIs to Future-based ones. For example,
   * parameter `keepPromise` can call some callback-based API, and the callback
   * implementation can complete the supplied promise.
   *
   * @param keepPromise code that completes the promise
   * @tparam T type of value to complete promise with
   * @return the future completion value
   */
  def promisedFuture[T](keepPromise: Promise[T] => Unit): Future[T] = {
    val p = promise[T]()
    keepPromise(p)
    p.future
  }

  def buildResponseHandler(promise: Promise[HttpResponse]) = new AsyncHttpResponseHandler {
    override def onSuccess(statusCode: Int, content: String) {
      promise success HttpResponse(statusCode, Option(content))
    }

    override def onFailure(t: Throwable, content: String) {
      //      val resp = Option(content) getOrElse "No response content."
      promise failure handleFailure(t, content)
    }
  }

  def handleFailure(t: Throwable, maybeContent: String): Throwable = t

  def uri(resource: String) = baseUrl + resource

  def getJson[T](resource: String)(implicit fjs: Reads[T]): Future[T] =
    get(resource).map(response => response.content.map(c => {
      //      info(s"Parsing: $c")
      try {
        Json.parse(c).as[T]
      } catch {
        case jre: JsResultException =>
          warn(s"Unable to parse: $c", jre)
          throw jre
      }
    }).getOrElse(throw new AndroidException("HTTP response content is empty, expected JSON.")))
}

object Protocols extends Enumeration {
  type Protocol = Value
  val Http, Https = Value

  def withNameIgnoreCase(name: String) = name.toLowerCase match {
    case "http" => Http
    case "https" => Https
    case other => throw new NoSuchElementException(s"Unknown Protocols name: $other")
  }
}

