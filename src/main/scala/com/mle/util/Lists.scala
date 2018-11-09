package com.mle.util

object Lists {
  def interleave[T](left: List[T], right: List[T]): List[T] =
    if (left.isEmpty) right
    else if (right.isEmpty) left
    else left.head :: right.head :: interleave(left.tail, right.tail)
}
