package com.mle.android.ui.activities

import android.os.Bundle
import android.app.Activity

/**
 *
 * @author mle
 */
trait BaseActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    onCreate2(Option(savedInstanceState))
  }

  /**
   * Convenience alternative to `onCreate`: super has already been called and the
   * parameter is wrapped in an [[scala.Option]] and guaranteed not to be null.
   *
   * @param state the state, wrapped in an [[scala.Option]]
   */
  protected def onCreate2(state: Option[Bundle]): Unit = {}
}
