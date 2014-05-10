package com.mle.android.ui.fragments

import android.support.v4.app.Fragment
import android.view.View
import com.mle.android.ui.ActivityUtils
import android.app.Activity

/**
 *
 * @author mle
 */
trait BaseFragment extends Fragment with ActivityUtils {
  def activity: Activity = getActivity

  def findViewById(id: Int): View = activity findViewById id
}
