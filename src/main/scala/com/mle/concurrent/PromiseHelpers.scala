package com.mle.concurrent

import scala.concurrent.Promise
import scala.util.Try

trait PromiseHelpers {
  // Defensive coding

  def trySuccess[T](value: T, promise: Promise[T]): Unit =
    tryIfPossible(promise)(_ trySuccess value)

  def tryFailure(t: Throwable, promises: Promise[_]*): Unit =
    promises foreach (p => tryIfPossible(p)(_ tryFailure t))

  def tryIfPossible[T, U](promise: Promise[T])(f: Promise[T] => U): Option[Try[U]] = {
    if (!promise.isCompleted) {
      Some(Try(f(promise)))
    } else {
      None
    }
  }
}