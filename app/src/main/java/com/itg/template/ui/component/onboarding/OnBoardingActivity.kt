package com.itg.template.ui.component.onboarding

import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.itg.template.R
import com.itg.template.ads.AdsManager
import com.itg.template.ads.RemoteConfigUtils
import com.itg.template.data.event.EventTracking
import com.itg.template.databinding.ActivityOnboardingBinding
import com.itg.template.ui.bases.BaseActivity
import com.itg.template.ui.bases.ext.isNetwork
import com.itg.template.ui.component.onboarding.adapter.OnboardingAdapter
import com.itg.template.ui.component.onboarding.model.OnboardingItem
import com.itg.template.ui.component.onboarding.viewmodel.OnboardingViewModel
import com.itg.template.utils.ITGTrackingHelper
import com.itg.template.utils.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity<ActivityOnboardingBinding>() {

    override val shouldShowNavigationBars = RemoteConfigUtils.getOnShowNavigationButton()

    override fun getLayoutActivity(): Int = R.layout.activity_onboarding

    private val onboardingViewModel by viewModels<OnboardingViewModel>()
    private lateinit var onboardingAdapter: OnboardingAdapter
    private val onboardingItems = mutableListOf<OnboardingItem>()

    override fun initViews() {
        initPage()
        initOnboardingItems()

        mBinding.root.postDelayed({
            AdsManager.loadNativeOnboarding4(
                this,
                appSharedPref.firstOnBoarding,
                R.layout.layout_native_onboarding
            )
            AdsManager.loadNativeOnboardingFull(
                this,
                appSharedPref.firstOnBoarding,
                R.layout.layout_native_onboarding_full
            )

            AdsManager.loadInterOnboarding(this)

        }, 100L)
    }


    override fun observerData() {
        super.observerData()
        onboardingViewModel.isNeedNextPage.observe(this) {
            val currentPosition = mBinding.viewPager.currentItem
            if (currentPosition < onboardingAdapter.itemCount - 1) {
                mBinding.viewPager.currentItem = currentPosition + 1
            } else startNextActivity()
        }
    }

    private fun initPage() {
        onboardingAdapter = OnboardingAdapter(supportFragmentManager, lifecycle)
        mBinding.viewPager.adapter = onboardingAdapter
        mBinding.viewPager.clipToPadding = false
        mBinding.viewPager.clipChildren = false
        mBinding.viewPager.offscreenPageLimit = 4
        mBinding.viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_ALWAYS

        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Map from the page itself: the full-screen native page is optional, so positions shift.
                val item = onboardingItems.getOrNull(position) ?: return
                val event = if (item.isHasNativeFull) {
                    val fullIndex = onboardingItems.take(position + 1).count { it.isHasNativeFull }
                    if (fullIndex <= 1) EventTracking.ONBOARDING_FULL1 else EventTracking.ONBOARDING_FULL2
                } else when (item.positionIndicator) {
                    1 -> EventTracking.ONBOARDING_SECOND
                    2 -> EventTracking.ONBOARDING_THIRD
                    3 -> EventTracking.ONBOARDING_4th
                    else -> EventTracking.ONBOARDING_FIRST
                }
                ITGTrackingHelper.logEvent(event, null)
            }
        })
    }

    private fun initOnboardingItems() {
        onboardingItems.clear()
        onboardingItems.add(
            OnboardingItem(
                title = R.string.onboarding_title_1,
                description = R.string.onboarding_des_1,
                textButton = R.string.next,
                imageResId = R.drawable.img_onboarding_1,
                positionIndicator = 0,
                isHasNativeOnPage1 = true
            )
        )
        onboardingItems.add(
            OnboardingItem(
                title = R.string.onboarding_title_2,
                description = R.string.onboarding_des_2,
                textButton = R.string.next,
                imageResId = R.drawable.img_onboarding_2,
                positionIndicator = 1
            )
        )

        if (isNetwork(this@OnBoardingActivity) &&
            AdsManager.shouldShowNativeOnboardingFull(appSharedPref.firstOnBoarding)
        ) {
            onboardingItems.add(
                OnboardingItem(
                    isHasNativeFull = true
                )
            )
        }

        onboardingItems.add(
            OnboardingItem(
                title = R.string.onboarding_title_3,
                description = R.string.onboarding_des_3,
                textButton = R.string.next,
                imageResId = R.drawable.img_onboarding_3_compare,
                positionIndicator = 2,
            )
        )

        onboardingItems.add(
            OnboardingItem(
                title = R.string.onboarding_title_4,
                description = R.string.onboarding_des_4,
                textButton = R.string.start_label,
                imageResId = R.drawable.img_onboarding_4,
                positionIndicator = 3,
                isHasNativeOnPage4 = true
            )
        )
        onboardingAdapter.submitData(onboardingItems)
    }

    private fun startNextActivity() {
        appSharedPref.firstOnBoarding = false
        AdsManager.showInterOnboarding(this) {
            Routes.startMainActivity(this)
            finish()
        }
    }

    override fun onBackPressed() {
        // block back press on onboarding
    }
}
