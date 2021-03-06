package com.malliina.android.ui.fragments

import android.support.v4.app.Fragment
import android.view.View
import com.malliina.android.ui.ActivityUtils
import android.app.Activity

trait BaseFragment extends Fragment with ActivityUtils {
  def activity: Activity = getActivity

  def findViewById(id: Int): View = activity.findViewById[View](id)
}
