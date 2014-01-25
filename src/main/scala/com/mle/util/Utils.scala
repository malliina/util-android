package com.mle.util

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

/**
 *
 * @author mle
 */
object Utils {
  implicit val executionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(50))

  def runnable(f: => Any): Runnable = new Runnable {
    def run() {
      f
    }
  }
}
