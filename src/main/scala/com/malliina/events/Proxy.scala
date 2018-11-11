package com.malliina.events

import com.malliina.android.events.EventSource

/** TODO rename to avoid clashes with scala.Proxy
  */
trait Proxy[T] {
  protected val source: EventSource[T]

  protected val handler: PartialFunction[T, Unit]
}
