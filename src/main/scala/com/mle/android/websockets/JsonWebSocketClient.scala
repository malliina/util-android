package com.mle.android.websockets

import collection.JavaConversions._
import java.net.URI
import org.java_websocket.client.{DefaultSSLWebSocketClientFactory, WebSocketClient}
import org.java_websocket.drafts
import org.java_websocket.handshake.ServerHandshake
import play.api.libs.json.{Writes, Json, JsValue}
import scala.concurrent.Future
import com.mle.android.http.{MySslSocketFactory, HttpConstants, HttpUtil}
import com.mle.android.util.UtilLog
import com.mle.android.exceptions.NotConnectedException


/**
 *
 * @author mle
 */
class JsonWebSocketClient(uri: String, username: String, password: String, additionalHeaders: (String, String)*)
  extends JsonWebSocket with UtilLog {

  private val connectPromise = concurrent.promise[Unit]()

  private val headers = Map(
    HttpConstants.AUTHORIZATION -> HttpUtil.authorizationValue(username, password)
  ) ++ additionalHeaders.toMap

  val client = new WebSocketClient(URI create uri, new drafts.Draft_10, headers, 0) {
    def onOpen(handshakedata: ServerHandshake) {
      debug(s"Opened websocket to: $uri")
      connectPromise.trySuccess(())
    }

    def onMessage(message: String): Unit = {
      //      info(s"Message: $message")
      JsonWebSocketClient.this.onMessage(Json.parse(message))
    }

    def onClose(code: Int, reason: String, remote: Boolean) {
      debug(s"Closed websocket to: $uri, code: $code, reason: $reason")
      connectPromise.tryFailure(new NotConnectedException(s"The websocket was closed. Code: $code, reason: $reason."))
    }

    /**
     * Exceptions thrown in this handler like in onMessage end up here.
     */
    def onError(ex: Exception) {
      warn("WebSocket error", ex)
      connectPromise.tryFailure(ex)
    }
  }
  if (uri startsWith "wss") {
    // makes SSL-encrypted websockets work with self-signed server certificates
    val factory = new DefaultSSLWebSocketClientFactory(MySslSocketFactory.trustAllSslContext())
    client setWebSocketFactory factory
  }

  def close(): Unit = client.close()

  def isConnected = client.getConnection.isOpen

  /**
   * Reconnections are currently not supported; only call this method once per instance.
   *
   * Impl: On subsequent calls, the returned future will always completed regardless of
   * connection result
   *
   * @return a future that completes when the connection has successfully been established
   */
  def connect: Future[Unit] = {
    client.connect()
    connectPromise.future
  }

  def send[T](message: T)(implicit writer: Writes[T]): Unit =
    send(Json toJson message)

  def send(json: JsValue): Unit =
    client send (Json stringify json)

  def onMessage(json: JsValue) = ()
}


