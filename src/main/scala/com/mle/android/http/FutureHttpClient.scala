package com.mle.android.http

import java.io.{Closeable, File}

import com.loopj.android.http._
import com.mle.android.exceptions.{NotFoundHttpException, UnauthorizedHttpException}
import com.mle.android.util.UtilLog
import com.mle.concurrent.ExecutionContexts.cached
import com.mle.concurrent.Futures
import org.apache.http.Header
import org.apache.http.client.HttpResponseException

import scala.concurrent.{Future, Promise}
import scala.util.Try

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
   * The returned [[Future]] fails with an [[java.io.IOException]] if the server cannot be reached, an
   * [[UnauthorizedHttpException]] if authentication fails and a [[NotFoundHttpException]] if the server responds with a
   * 404. For other errors it may fail with a [[HttpResponseException]] containing the appropriate error code.
   *
   * @param uri request uri
   * @return the HTTP response following a successful request
   */
  def get(uri: String): Future[HttpResponse] = get(uri, textResponseHandler)

  def get[T](uri: String, f: Promise[T] => AsyncHttpResponseHandler): Future[T] =
    Futures.promisedFuture[T](p => httpClient.get(transformUri(uri), f(p)))

  def getFile(uri: String, file: File): Future[File] = get[File](uri, p => fileResponseHandler(file, p))

  def postFile(resource: String, file: File): Future[HttpResponse] = postFile(resource, file, textResponseHandler)

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

  def textResponseHandler(promise: Promise[HttpResponse]) = new TextHttpResponseHandler() {
    override def onSuccess(statusCode: Int, headers: Array[Header], responseString: String): Unit = {
      promise success HttpResponse(statusCode, Option(responseString))
    }

    override def onFailure(statusCode: Int, headers: Array[Header], responseString: String, throwable: Throwable): Unit = {
      promise failure handleFailure(throwable, Option(responseString))
    }
  }

  def fileResponseHandler(file: File, promise: Promise[File]) = new FileAsyncHttpResponseHandler(file) {
    override def onSuccess(statusCode: Int, headers: Array[Header], file: File): Unit = {
      promise success file
    }

    override def onFailure(statusCode: Int, headers: Array[Header], throwable: Throwable, file: File): Unit = {
      promise failure handleFailure(throwable, None)
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