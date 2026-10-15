package com.itg.template.ui.component.onboarding.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.ads.module.ads.wrapper.ApNativeAd
import com.bumptech.glide.Glide
import com.itg.template.R
import com.itg.template.ads.AdsManager
import com.itg.template.ads.populateNativeAdView
import com.itg.template.databinding.FragmentOnboardingPageBinding
import com.itg.template.ui.bases.BaseFragment
import com.itg.template.ui.bases.ext.click
import com.itg.template.ui.bases.ext.goneView
import com.itg.template.ui.bases.ext.invisibleView
import com.itg.template.ui.bases.ext.parcelable
import com.itg.template.ui.bases.ext.visibleView
import com.itg.template.ui.component.onboarding.model.OnboardingItem
import com.itg.template.ui.component.onboarding.viewmodel.OnboardingViewModel

class OnboardingPageFragment : BaseFragment<FragmentOnboardingPageBinding>() {

    override fun getLayoutFragment(): Int = R.layout.fragment_onboarding_page

    companion object {
        private const val ARG_ONBOARDING_ITEM = "arg_onboarding_item"

        fun newInstance(onboardingItem: OnboardingItem) = OnboardingPageFragment().apply {
            arguments = bundleOf(ARG_ONBOARDING_ITEM to onboardingItem)
        }
    }

    private val onboardingViewModel by activityViewModels<OnboardingViewModel>()

    private var onboardingItem: OnboardingItem = OnboardingItem(
        title = R.string.onboarding_title_1,
        description = R.string.onboarding_des_1,
        textButton = R.string.next,
        imageResId = R.drawable.ic_vietnamese,
        positionIndicator = 0
    )

    override fun initViews() {
        arguments?.parcelable<OnboardingItem>(ARG_ONBOARDING_ITEM)?.let {
            onboardingItem = it
        }
        updateLayout()
        if (onboardingItem.isHasNativeFull) {
            mBinding.layoutAdsFull.visibleView()
        } else {
            mBinding.layoutAdsFull.invisibleView()
        }
    }

    override fun observerData() {
        observeAdChannel()
    }

    override fun onClickViews() {
        mBinding.btnNext.click { onboardingViewModel.onNextClicked() }
        mBinding.imgCloseAdsFull.click { onboardingViewModel.onNextClicked() }
    }

    private fun observeAdChannel() {
        val liveData: MutableLiveData<ApNativeAd?> = when {
            onboardingItem.isHasNativeOnPage1 -> AdsManager.nativeOnboarding1AdLive
            onboardingItem.isHasNativeOnPage4 -> AdsManager.nativeOnboarding4AdLive
            onboardingItem.isHasNativeFull -> AdsManager.nativeAdOnBoardingFullLive
            else -> {
                renderNoAd()
                return
            }
        }
        liveData.observe(viewLifecycleOwner) { ad -> renderAd(ad) }
    }

    private fun renderAd(ad: ApNativeAd?) {
        if (onboardingItem.isHasNativeFull) {
            if (ad != null) {
                mBinding.layoutAdsFull.visibleView()
                mBinding.imgCloseAdsFull.visibleView()
                mBinding.layoutContent.invisibleView()
                populateNativeAdView(
                    requireActivity(),
                    ad,
                    mBinding.layoutAdsFull,
                    mBinding.shimmerAdsFull.shimmerNativeFull
                )
            } else {
                mBinding.layoutContent.visibleView()
                mBinding.layoutAdsFull.invisibleView()
                mBinding.layoutAds.invisibleView()
                mBinding.imgCloseAdsFull.invisibleView()
            }
        } else if (onboardingItem.isHasNativeOnPage1 || onboardingItem.isHasNativeOnPage4) {
            if (ad != null) {
                mBinding.layoutAds.visibleView()
                populateNativeAdView(
                    requireActivity(),
                    ad,
                    mBinding.layoutAds,
                    mBinding.shimmerAds.shimmerNativeMedium
                )
            } else {
                mBinding.layoutAds.invisibleView()
            }
            mBinding.layoutContent.visibleView()
            mBinding.layoutAdsFull.invisibleView()
            mBinding.imgCloseAdsFull.invisibleView()
        }
    }

    private fun renderNoAd() {
        mBinding.layoutContent.visibleView()
        mBinding.layoutAds.goneView()
        mBinding.layoutAdsFull.invisibleView()
        mBinding.imgCloseAdsFull.invisibleView()
    }

    private fun updateLayout() {
        Glide.with(this).load(onboardingItem.imageResId).into(mBinding.imgOnboarding)
        mBinding.tvTitle.text = getString(onboardingItem.title)
        mBinding.tvDes.text = getString(onboardingItem.description)
        mBinding.btnNext.text = getString(onboardingItem.textButton)
        mBinding.imgIndicator0.setImageResource(R.drawable.ic_onboarding_indicator)
        mBinding.imgIndicator1.setImageResource(R.drawable.ic_onboarding_indicator)
        mBinding.imgIndicator2.setImageResource(R.drawable.ic_onboarding_indicator)
        mBinding.imgIndicator3.setImageResource(R.drawable.ic_onboarding_indicator)
        when (onboardingItem.positionIndicator) {
            0 -> mBinding.imgIndicator0.setImageResource(R.drawable.ic_onboarding_indicator_selected)
            1 -> mBinding.imgIndicator1.setImageResource(R.drawable.ic_onboarding_indicator_selected)
            2 -> mBinding.imgIndicator2.setImageResource(R.drawable.ic_onboarding_indicator_selected)
            3 -> mBinding.imgIndicator3.setImageResource(R.drawable.ic_onboarding_indicator_selected)
        }
        applyPageOverlays(onboardingItem.positionIndicator)
    }

    private fun applyPageOverlays(page: Int) {
        mBinding.overlayBadge.visibleView()
        mBinding.overlayOriginal.goneView()
        mBinding.overlayPlay.goneView()
        mBinding.overlayTimeline.goneView()
        mBinding.overlayBeforeDim.goneView()
        mBinding.overlayDivider.goneView()
        mBinding.tvBefore.goneView()
        mBinding.tvAfter.goneView()
        mBinding.overlayThumbs.goneView()
        mBinding.overlayChips.goneView()

        when (page) {
            0 -> {
                mBinding.tvBadge.text = "AI Templates"
                mBinding.chip1.text = "Anime"
                mBinding.chip2.text = "Oil Paint"
                mBinding.chip3.text = "3D Cartoon"
                mBinding.overlayOriginal.visibleView()
                mBinding.overlayChips.visibleView()
            }
            1 -> {
                mBinding.tvBadge.text = "Image → Video"
                mBinding.overlayPlay.visibleView()
                mBinding.overlayTimeline.visibleView()
            }
            2 -> {
                mBinding.tvBadge.text = "Upscale to HD"
                mBinding.overlayBeforeDim.visibleView()
                mBinding.overlayDivider.visibleView()
                mBinding.tvBefore.visibleView()
                mBinding.tvAfter.visibleView()
            }
            3 -> {
                mBinding.tvBadge.text = "Toonify & Effects"
                mBinding.chip1.text = "Toonify"
                mBinding.chip2.text = "Hair Colour"
                mBinding.chip3.text = "Trending"
                mBinding.overlayThumbs.visibleView()
                mBinding.overlayChips.visibleView()
            }
            else -> mBinding.overlayBadge.goneView()
        }
    }
}
