package com.malliina.concurrent

import java.util.concurrent._

import com.malliina.util.Utils

import scala.concurrent.duration.Duration

object Scheduling extends Scheduling(Executors.newSingleThreadScheduledExecutor())

class Scheduling(executor: ScheduledExecutorService) {
  def every(interval: Duration)(code: => Unit): ScheduledFuture[_] = {
    val intervalMillis = interval.toMillis
    executor.scheduleWithFixedDelay(Utils.runnable(code), 1, intervalMillis, TimeUnit.MILLISECONDS)
  }

  def runnable(code: => Unit) = new Runnable {
    def run(): Unit = code
  }

  def callable[T](code: => T) = new Callable[T] {
    def call(): T = code
  }

  def shutdown(): Unit = executor.shutdown()
}
