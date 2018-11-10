package com.mle.android.ui

import android.content.DialogInterface
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.{Adapter, SeekBar, AdapterView}

object Implicits {

  implicit class RichAdapterView[T <: Adapter](val listener: AdapterView[T]) {
    def setListener(f: () => Any): Unit = setListener(_ => f())

    def setListener(f: View => Any): Unit =
      listener setOnClickListener new OnClickListener {
        override def onClick(v: View): Unit = f(v)
      }
  }

  implicit def action2clickListener(f: View => Any): OnClickListener = {
    new OnClickListener {
      def onClick(v: View): Unit = f(v)
    }
  }

  implicit def action2clickListener(f: () => Any): OnClickListener = {
    action2clickListener(v => f())
  }

  implicit def action2itemClickListener(f: (AdapterView[_], View, Int, Long) => Unit): OnItemClickListener =
    new OnItemClickListener {
      def onItemClick(av: AdapterView[_], v: View, position: Int, id: Long) {
        f(av, v, position, id)
      }
    }

  implicit def action2itemClickListener2(f: (AdapterView[_], Int) => Unit): OnItemClickListener =
    new OnItemClickListener {
      def onItemClick(av: AdapterView[_], v: View, position: Int, id: Long): Unit =
        f(av, position)
    }

  implicit def fun2seekChangeListener(f: (SeekBar, Int) => Unit): SeekBar.OnSeekBarChangeListener = new OnSeekBarChangeListener {
    def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
      if (fromUser) {
        f(seekBar, progress)
      }
    }

    def onStopTrackingTouch(seekBar: SeekBar) {

    }

    def onStartTrackingTouch(seekBar: SeekBar) {}
  }

  implicit def action2prefClickListener(f: Preference => Boolean) = new OnPreferenceClickListener {
    def onPreferenceClick(preference: Preference): Boolean = f(preference)
  }

  implicit def action2itemSelectedListener(f: (AdapterView[_], Int) => Unit) = new AdapterView.OnItemSelectedListener {
    def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long): Unit =
      f(parent, position)

    def onNothingSelected(parent: AdapterView[_]): Unit = ()
  }

  implicit def action2itemSelectedListener[T](f: T => Unit): AdapterView.OnItemSelectedListener =
    action2itemSelectedListener((av, pos) => f(av.getItemAtPosition(pos).asInstanceOf[T]))

  implicit def clickListener(f: (DialogInterface, Int) => Any): DialogInterface.OnClickListener =
    new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) = f(dialog, which)
    }
}
