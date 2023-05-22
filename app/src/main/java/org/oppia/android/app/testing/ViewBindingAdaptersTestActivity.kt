package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test activity for ViewBindingAdapters. */
class ViewBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.activity_view_binding_adapters_test)
  }

  /** Dagger injector for [ViewBindingAdaptersTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: ViewBindingAdaptersTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [ViewBindingAdaptersTestActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, ViewBindingAdaptersTestActivity::class.java)
  }
}
