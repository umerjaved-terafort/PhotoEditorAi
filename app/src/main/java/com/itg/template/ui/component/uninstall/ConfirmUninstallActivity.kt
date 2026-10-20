package com.itg.template.ui.component.uninstall

import android.widget.FrameLayout
import com.ads.module.ads.wrapper.ApNativeAd
import com.facebook.shimmer.ShimmerFrameLayout
import com.itg.template.R
import com.itg.template.data.event.EventTracking
import com.itg.template.utils.ITGTrackingHelper
import com.itg.template.ads.AdRemoteConfig
import com.itg.template.ads.AdsManager
import com.itg.template.ads.RemoteConfigUtils
import com.itg.template.ads.populateNativeAdView
import com.itg.template.databinding.ActivityConfirmUninstallBinding
import com.itg.template.ui.bases.BaseActivity
import com.itg.template.ui.bases.ext.click
import com.itg.template.ui.bases.ext.goneView
import com.itg.template.ui.bases.ext.visibleView
import com.itg.template.utils.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmUninstallActivity : BaseActivity<ActivityConfirmUninstallBinding>() {

    override fun getLayoutActivity() = R.layout.activity_confirm_uninstall

    override fun initViews() {
        super.initViews()
        ITGTrackingHelper.logEvent(EventTracking.UNINSTALL_CONFIRM, null)
        initRemoteConfigIfOpenedDirectly()
        AdsManager.loadNativeConfirmUninstall(this, R.layout.layout_native_ad_large)
    }

    /**
     * The Uninstall shortcut opens this screen directly (Splash is skipped), so on a cold start
     * Remote Config was never fetched. Ads here use the bundled config straight away; the fetch
     * runs in the background and updates AdRemoteConfig for the Survey screen.
     */
    private fun initRemoteConfigIfOpenedDirectly() {
        if (RemoteConfigUtils.completed) return
        val appContext = applicationContext
        RemoteConfigUtils.init(appContext, object : RemoteConfigUtils.Listener {
            override fun loadSuccess() {
                AdRemoteConfig.initialize(appContext, RemoteConfigUtils.getAdRemoteConfig())
            }
        })
    }

    override fun observerData() {
        super.observerData()
        AdsManager.nativeConfirmUninstallAdLive.observe(this) { ad ->
            renderConfirmUninstallAd(ad)
        }
    }

    override fun onClickViews() {
        super.onClickViews()

        mBinding.imgBack.click {
            whenBack()
        }

        mBinding.btnTryAgain.click {
            whenBack()
        }

        mBinding.btnStillUninstall.click {
            Routes.startSurveyActivity(this)
            finish()
        }

    }


    private fun whenBack() {
        Routes.startMainActivity(this)
        finish()
    }

    private fun renderConfirmUninstallAd(ad: ApNativeAd?) {
        val frAds = mBinding.root.findViewById<FrameLayout>(R.id.fr_ads) ?: return
        if (ad == null) {
            frAds.goneView()
            return
        }
        frAds.visibleView()
        val shimmer = mBinding.root.findViewById<ShimmerFrameLayout>(R.id.shimmer_ads)
        if (shimmer != null) {
            populateNativeAdView(this, ad, frAds, shimmer)
        }
    }

    override fun onActivityBackPressed() {
        whenBack()
    }
}
