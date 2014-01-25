package com.mle.android.ui.fragments

import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle

/**
 *
 * @author mle
 */
trait FragmentBase extends FragmentHelpers {
  def layoutId: Int

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View =
    inflater.inflate(layoutId, container, false)

  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)
    initViews(Option(savedInstanceState))
  }

  /**
   * Called when the [[android.app.Activity]] this fragment belongs to has been created.
   *
   * @param savedInstanceState  state
   */
  def initViews(savedInstanceState: Option[Bundle]): Unit = {}
}
