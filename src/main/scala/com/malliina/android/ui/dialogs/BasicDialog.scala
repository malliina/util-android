package com.malliina.android.ui.dialogs

import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.app.Dialog
import play.api.libs.json.{Format, Json}

/**
  * @tparam T type of state
  */
trait BasicDialog[T] extends DialogFragment {
  def saveState(state: Bundle): Unit

  def restoreState(stateOpt: Option[Bundle]): Option[T]

  def onCreateDialog2(stateOpt: Option[Bundle], restoredState: Option[T]): Dialog

  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    saveState(outState)
  }

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val state = Option(savedInstanceState)
    val restored = restoreState(state)
    onCreateDialog2(state, restored)
  }
}

abstract class JsonDialog[T](item: T, stateKey: String)(implicit tjs: Format[T]) extends BasicDialog[T] {
  override def restoreState(stateOpt: Option[Bundle]): Option[T] =
    for {
      state <- stateOpt
      json <- Option(state getString stateKey)
      restored <- Json.parse(json).asOpt[T]
    } yield restored

  override def saveState(state: Bundle): Unit =
    state putString(stateKey, Json.stringify(Json.toJson(item)))
}