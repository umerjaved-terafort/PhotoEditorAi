package com.itg.template.ui.component.onboarding.adapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.TypedValue
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
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
            mBinding.imgCloseAdsFull.visibleView()
            mBinding.layoutContent.invisibleView()
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
        applyHeroHeight(onboardingItem.positionIndicator)
        applyPageOverlays(onboardingItem.positionIndicator)
    }

    /**
     * Design hero heights (below the status bar): 344/778 for Intro 1 & 4, 488/778 for Intro 2 & 3.
     * Percent is relative to the full page so the ad card below doesn't shrink the hero.
     */
    private fun applyHeroHeight(page: Int) {
        mBinding.heroFrame.updateLayoutParams<ConstraintLayout.LayoutParams> {
            matchConstraintPercentHeight = if (page == 1 || page == 2) 0.62f else 0.44f
        }
        mBinding.heroFrame.setBackgroundResource(
            when (page) {
                1 -> R.drawable.bg_onb_hero_pink
                2 -> 0
                else -> R.drawable.bg_onb_hero_lilac
            }
        )
        // Intro 4 title sits 26dp below the hero, others 28dp.
        mBinding.tvTitle.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = dp(if (page == 3) 26f else 28f)
        }
    }

    private fun applyPageOverlays(page: Int) {
        mBinding.overlayBadge.visibleView()
        mBinding.overlayOriginal.goneView()
        mBinding.overlayPlay.goneView()
        mBinding.overlayTimeline.goneView()
        mBinding.overlayBeforeDim.goneView()
        mBinding.overlayDivider.goneView()
        mBinding.overlayCompare.goneView()
        mBinding.tvBefore.goneView()
        mBinding.tvAfter.goneView()
        mBinding.overlayThumbs.goneView()
        mBinding.overlayChips.goneView()

        when (page) {
            0 -> {
                mBinding.imgBadgeIcon.setImageResource(R.drawable.ic_onb_badge_sparkle)
                mBinding.tvBadge.setText(R.string.onb_badge_ai_templates)
                mBinding.chip1.setText(R.string.onb_chip_anime)
                mBinding.chip2.setText(R.string.onb_chip_oil_paint)
                mBinding.chip3.setText(R.string.onb_chip_3d_cartoon)
                setChipsHorizontalBias(0f)
                mBinding.overlayOriginal.visibleView()
                mBinding.overlayChips.visibleView()
            }
            1 -> {
                mBinding.imgBadgeIcon.setImageResource(R.drawable.ic_onb_badge_video)
                mBinding.tvBadge.setText(R.string.onb_badge_image_to_video)
                mBinding.overlayPlay.visibleView()
                mBinding.overlayTimeline.visibleView()
            }
            2 -> {
                mBinding.imgBadgeIcon.setImageResource(R.drawable.ic_onb_badge_upscale)
                mBinding.tvBadge.setText(R.string.onb_badge_upscale_hd)
                mBinding.overlayBeforeDim.visibleView()
                mBinding.overlayDivider.visibleView()
                mBinding.overlayCompare.visibleView()
                mBinding.tvBefore.visibleView()
                mBinding.tvAfter.visibleView()
                setupBeforeHalf()
            }
            3 -> {
                // Intro 4 has no top badge in the design.
                mBinding.overlayBadge.goneView()
                mBinding.chip1.setText(R.string.onb_chip_toonify)
                mBinding.chip2.setText(R.string.onb_chip_hair_colour)
                mBinding.chip3.setText(R.string.onb_chip_trending)
                setChipsHorizontalBias(0.5f)
                mBinding.overlayThumbs.visibleView()
                mBinding.overlayChips.visibleView()
            }
            else -> mBinding.overlayBadge.goneView()
        }
    }

    private fun setChipsHorizontalBias(bias: Float) {
        mBinding.overlayChips.updateLayoutParams<ConstraintLayout.LayoutParams> {
            horizontalBias = bias
        }
    }

    /**
     * Intro 3 "before" half: same hero image, blurred (API 31+) and desaturated, clipped to the
     * left of the divider. The inner image is sized to the full hero so it lines up with the "after" image.
     */
    private fun setupBeforeHalf() {
        val before = mBinding.imgBeforeBlur
        Glide.with(this).load(onboardingItem.imageResId).into(before)
        before.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setSaturation(0.7f)
            postConcat(ColorMatrix().apply { setScale(0.95f, 0.95f, 0.95f, 1f) })
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val radius = dp(4f).toFloat()
            before.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP))
        }
        mBinding.heroFrame.doOnLayout { hero ->
            before.updateLayoutParams {
                width = hero.width
                height = hero.height
            }
        }
    }

    private fun dp(value: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()
}
