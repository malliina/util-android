package com.mle.android.http

import com.loopj.android.http.{RequestParams, AsyncHttpResponseHandler, AsyncHttpClient}
import scala.concurrent.{Promise, Future}
import com.mle.concurrent.Futures
import org.apache.http.client.HttpResponseException
import com.mle.android.exceptions.{NotFoundHttpException, UnauthorizedHttpException}
import com.mle.android.util.UtilLog
import java.io.{File, Closeable}
import scala.util.Try
import com.mle.util.Utils.executionContext

/**
 *
 * @author mle
 */
trait FutureHttpClient extends UtilLog with Closeable {
  val httpClient = new AsyncHttpClient()
  httpClient setSSLSocketFactory MySslSocketFactory.allowAllCertificatesSocketFactory()

  //  the max parallel connections count seems capped at 10 per client, and none of the following have an effect on that
  //  httpClient setThreadPool Executors.newCachedThreadPool().asInstanceOf[ThreadPoolExecutor]
  //  httpClient setMaxConnections 256
  //  ConnManagerParams.setMaxTotalConnections(httpClient.getHttpClient.getParams, 256)

  def addHeaders(headers: (String, String)*) = headers.foreach {
    case (key, value) => httpClient.addHeader(key, value)
  }

  /**
   * Gets called before the request is executed.
   *
   * The default implementation trivially returns `uri`.
   *
   * @param uri the user-supplied uri string
   * @return the uri actually used in the request
   */
  def transformUri(uri: String): String = uri

  def getEmpty(uri: String) = get(uri).map(_ => ())

  /**
   * GETs `uri`.
   *
   * The returned [[Future]] fails with an [[java.io.IOException]] if
   * the server cannot be reached, an [[UnauthorizedHttpException]] if
   * authentication fails and a [[NotFoundHttpException]] if the server
   * responds with a 404. For other errors it may fail with a
   * [[HttpResponseException]] containing the appropriate error code.
   *
   * @param uri request uri
   * @return the HTTP response following a successful request
   */
  def get(uri: String): Future[HttpResponse] = get(uri, buildResponseHandler)

  def get[T](uri: String, f: Promise[T] => AsyncHttpResponseHandler): Future[T] =
    Futures.promisedFuture[T](p => httpClient.get(transformUri(uri), f(p)))

  def postFile(resource: String, file: File): Future[HttpResponse] =
    postFile(resource, file, buildResponseHandler)

  /**
   * Performs a multipart/form-data upload of `file`.
   *
   * @param file file to upload
   */
  def postFile[T](uri: String, file: File, f: Promise[T] => AsyncHttpResponseHandler): Future[T] =
    Futures.promisedFuture[T](p => {
      val params = new RequestParams()
      params.put("file", file)
      httpClient.post(transformUri(uri), params, f(p))
    })

  def buildResponseHandler(promise: Promise[HttpResponse]) = new AsyncHttpResponseHandler {
    override def onSuccess(statusCode: Int, content: String): Unit = {
      //      info(s"Success: $content")
      promise success HttpResponse(statusCode, Option(content))
    }

    override def onFailure(t: Throwable, content: String): Unit = {
      //      info(s"Failure: ${t.getMessage}")
      promise failure handleFailure(t, Option(content))
    }
  }

  def handleFailure(t: Throwable, maybeContent: Option[String]): Throwable =
    httpFailureHandler.applyOrElse(t, (fail: Throwable) => fail)

  val httpFailureRefiner: PartialFunction[Throwable, Throwable] = {
    case hre: HttpResponseException if hre.getStatusCode == HttpConstants.UNAUTHORIZED =>
      new UnauthorizedHttpException(hre)
    case hre: HttpResponseException if hre.getStatusCode == HttpConstants.NOT_FOUND =>
      new NotFoundHttpException(hre)
  }
  val passThroughHandler: PartialFunction[Throwable, Throwable] = {
    case t: Throwable => t
  }
  val httpFailureHandler = httpFailureRefiner orElse passThroughHandler

  override def close(): Unit =
    Try(httpClient.getHttpClient.getConnectionManager.shutdown())
}