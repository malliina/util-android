package com.mle.android.http

trait IEndpoint {
  def name: String

  def host: String

  def port: Int

  def username: String

  def password: String

  def protocol: Protocols.Protocol
}
