package com.mle.android.http

import javax.net.ssl.{TrustManager, X509TrustManager, SSLContext}
import org.apache.http.conn.ssl.SSLSocketFactory
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.net.Socket

/**
 * A socket factory that trusts all certificates.
 *
 * Useful for building client applications that should work with
 * servers that run with self-signed certificates.
 *
 * @see http://stackoverflow.com/questions/11573108/self-signed-certificate-and-loopj-for-android
 *
 * @author mle
 */
class MySslSocketFactory(trust: KeyStore) extends SSLSocketFactory(trust) {
  val sslContext = MySslSocketFactory.trustAllSslContext()

  override def createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket =
    sslContext.getSocketFactory.createSocket(socket, host, port, autoClose)

  override def createSocket(): Socket = sslContext.getSocketFactory.createSocket()
}

object MySslSocketFactory {
  /**
   * Builds and initializes an [[javax.net.ssl.SSLContext]] that
   * trusts all certificates. Use this with SSL-enabled clients
   * that speak to servers with self-signed certificates.
   *
   * MITM BLAH BLAH BLAH
   *
   * @return an SSL context that trusts all certificates
   */
  def trustAllSslContext() = {
    val sslContext = SSLContext.getInstance("TLS")
    val trustManager = MySslSocketFactory.trustAllTrustManager()
    sslContext.init(null, Array[TrustManager](trustManager), null)
    sslContext
  }

  def trustAllTrustManager() = new X509TrustManager() {
    override def checkClientTrusted(chain: Array[X509Certificate], authType: String) {
    }

    override def checkServerTrusted(chain: Array[X509Certificate], authType: String) {
    }

    override def getAcceptedIssuers: Array[X509Certificate] = null
  }

  /**
   *
   * @return a socket factory that trusts all server certificates
   */
  def allowAllCertificatesSocketFactory() = {
    val trustStore = KeyStore.getInstance(KeyStore.getDefaultType)
    trustStore.load(null, null)
    val socketFactory = new MySslSocketFactory(trustStore)
    socketFactory setHostnameVerifier SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
    socketFactory
  }
}