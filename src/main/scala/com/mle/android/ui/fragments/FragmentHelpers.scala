package com.mle.android.ui.fragments

import android.support.v4.app.Fragment
import android.view.View
import com.mle.android.ui.ActivityUtils

/**
 *
 * @author mle
 */
trait FragmentHelpers extends Fragment with ActivityUtils {
  def activity = getActivity

  def findViewById(id: Int): View = activity.findViewById(id)
}
