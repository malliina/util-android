package com.mle.events

import com.mle.android.events.EventSource

/** TODO rename to avoid clashes with scala.Proxy
  */
trait Proxy[T] {
  protected val source: EventSource[T]

  protected val handler: PartialFunction[T, Unit]
}