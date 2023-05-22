package org.oppia.android.app.policies

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.PoliciesActivityParams
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.model.ScreenName.POLICIES_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for displaying the app policies. */
class PoliciesActivity :
  InjectableAppCompatActivity(),
  RouteToPoliciesListener {

  @Inject
  lateinit var policiesActivityPresenter: PoliciesActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)

    policiesActivityPresenter.handleOnCreate(
      intent.getProtoExtra(
        POLICIES_ACTIVITY_POLICY_PAGE_PARAMS_PROTO,
        PoliciesActivityParams.getDefaultInstance()
      )
    )
  }

  companion object {
    /** Argument key for policy page in [PoliciesActivity]. */
    const val POLICIES_ACTIVITY_POLICY_PAGE_PARAMS_PROTO = "PoliciesActivity.policy_page"

    /** Returns the [Intent] for opening [PoliciesActivity] for the specified [policyPage]. */
    fun createIntent(context: Context, policyPage: PolicyPage): Intent {
      val policiesActivityParams =
        PoliciesActivityParams
          .newBuilder()
          .setPolicyPage(policyPage)
          .build()
      return Intent(context, PoliciesActivity::class.java).also {
        it.putProtoExtra(POLICIES_ACTIVITY_POLICY_PAGE_PARAMS_PROTO, policiesActivityParams)
        it.decorateWithScreenName(POLICIES_ACTIVITY)
      }
    }
  }

  override fun onRouteToPolicies(policyPage: PolicyPage) {
    startActivity(createIntent(this, policyPage))
  }

  /** Dagger injector for [PoliciesActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: PoliciesActivity)
  }
}
