package com.itg.template.ui.component.welcome

import com.ads.module.ads.wrapper.ApNativeAd
import com.itg.template.R
import com.itg.template.data.event.EventTracking
import com.itg.template.utils.ITGTrackingHelper
import com.itg.template.ads.AdsManager
import com.itg.template.ads.populateNativeAdView
import com.itg.template.app.AppConstants
import com.itg.template.databinding.ActivityWelcomeBinding
import com.itg.template.ui.bases.BaseActivity
import com.itg.template.ui.bases.ext.click
import com.itg.template.ui.bases.ext.goneView
import com.itg.template.ui.bases.ext.isNetwork
import com.itg.template.ui.bases.ext.visibleView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeActivity : BaseActivity<ActivityWelcomeBinding>() {

    override fun getLayoutActivity() = R.layout.activity_welcome

    override fun initViews() {
        super.initViews()
        ITGTrackingHelper.logEvent(EventTracking.WELCOME_BACK, null)
        AdsManager.loadNativeWelcome(this, R.layout.layout_native_welcome)
        AdsManager.loadInterWelcome(this)
        stateUi()
    }

    /**
     * Start stays in its loading state until inter_welcome_back has loaded or failed (or the max
     * wait passes). Enabling it after a fixed 500ms let users tap before the ad was ready, so the
     * inter was skipped (QA Ai Photo_14).
     */
    fun stateUi(){
        showStart(ready = false)
        mBinding.btnStart.postDelayed(
            { onInterResolved() },
            AppConstants.DEFAULT_TIME_DELAY_LOAD_INTER_WELCOME
        )
    }

    override fun observerData() {
        AdsManager.nativeWelcomeAdLive.observe(this) { renderWelcomeAd(it) }
        AdsManager.interWelcomeAdLive.observe(this) { onInterResolved() }
    }

    private var isInterResolved = false
    private var pendingLeave = false

    private fun onInterResolved() {
        if (isInterResolved) return
        isInterResolved = true
        showStart(ready = true)
        if (pendingLeave) leaveWithInter()
    }

    override fun onClickViews() {
        mBinding.btnStart.click {
            if (!mBinding.tvStart.isShown) return@click
            leaveWithInter()
        }
    }

    /** Back also shows inter_welcome_back (35s interval enforced by ERain) before returning Home. */
    override fun onActivityBackPressed() {
        leaveWithInter()
    }

    private var isLeaving = false

    private fun leaveWithInter() {
        if (isLeaving) return
        if (!isInterResolved) {
            // Back pressed while the inter is still loading: leave as soon as it resolves.
            pendingLeave = true
            return
        }
        isLeaving = true
        AdsManager.showInterWelcome(this) { finish() }
    }

    override fun onDestroy() {
        mBinding.btnStart.handler?.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
    private fun showStart(ready: Boolean) {
        if (isFinishing || isDestroyed || mBinding.tvStart.isShown) return
        if (!ready) {
            mBinding.progressStart.visibleView()
            mBinding.tvStart.goneView()
            return
        }
        mBinding.btnStart.handler?.removeCallbacksAndMessages(null)
        mBinding.progressStart.goneView()
        mBinding.tvStart.visibleView()
    }

    private fun renderWelcomeAd(ad: ApNativeAd?) {
        if (ad == null || !isNetwork()) {
            mBinding.frAds.goneView()
            return
        }
        mBinding.frAds.visibleView()
        populateNativeAdView(
            this,
            ad,
            mBinding.frAds,
            mBinding.shimmerAds.shimmerNativeLarge)
    }
}
