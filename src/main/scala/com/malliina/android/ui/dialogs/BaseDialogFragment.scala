package com.malliina.android.ui.dialogs

import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.app.{AlertDialog, Dialog}

trait BaseDialogFragment extends DialogFragment {
  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val builder = new AlertDialog.Builder(getActivity)
    prepareBuilder(builder, Option(savedInstanceState))
    builder.create()
  }

  def prepareBuilder(builder: AlertDialog.Builder, state: Option[Bundle]): Unit
}
