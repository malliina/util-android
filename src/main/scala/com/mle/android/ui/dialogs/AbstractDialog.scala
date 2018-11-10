package com.mle.android.ui.dialogs

import android.app.AlertDialog.Builder
import android.os.Bundle
import android.content.DialogInterface
import com.mle.android.ui.Implicits
import Implicits._

trait AbstractDialog extends BaseDialogFragment {
  def message: Int

  def title: Option[Int]

  def positiveText: Option[Int]

  def negativeText: Option[Int]

  def prepareBuilder(builder: Builder, state: Option[Bundle]): Unit = {
    builder setMessage message
    title foreach builder.setTitle
    positiveText.foreach(pos => builder setPositiveButton(pos, (_: DialogInterface, _: Int) => onPositive()))
    negativeText.foreach(pos => builder setNegativeButton(pos, (_: DialogInterface, _: Int) => onNegative()))
  }

  def onPositive(): Any = ()

  def onNegative(): Any = ()
}
