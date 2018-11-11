package com.malliina.android.events

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** Inspiration: https://lampsvn.epfl.ch/trac/scala/browser/scala/trunk/src/swing/scala/swing/Reactions.scala
  *
  * TODO use rx instead.
  *
  * @tparam T type of event
  */
trait EventSource[T] {
  type EventHandler = PartialFunction[T, Unit]

  private val handlers: mutable.Buffer[EventHandler] = new ListBuffer[EventHandler]

  /**
    * This is method protected so that only event sources may fire messages,
    * but anyone can listen by adding event handlers.
    *
    * @param event event to fire
    */
  protected def fire(event: T): Unit =
    handlers.filter(_.isDefinedAt(event)).foreach(h => h(event))

  /**
    * Remember to call `removeHandler`.
    *
    * @param handler
    */
  def addHandler(handler: EventHandler): Unit = handlers += handler

  def removeHandler(handler: EventHandler): Unit = handlers -= handler
}
