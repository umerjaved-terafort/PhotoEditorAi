package com.itg.template.ui.bases

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.ads.module.admob.Admob
import com.ads.module.ads.wrapper.ApInterstitialAd
import com.ads.module.funtion.AdCallback
import com.itg.template.R
import com.itg.template.app.AppConstants
import com.itg.template.data.pref.AppSharedPref
import com.itg.template.utils.ITGTrackingHelper
import com.itg.template.utils.Routes
import java.util.Locale
import javax.inject.Inject

abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity() {

    companion object {
        private const val TAG = "BaseActivity"
    }

    lateinit var mBinding: VB

    @Inject
    lateinit var appSharedPref: AppSharedPref

    protected open val shouldShowNavigationBars = false

    private var loadingView: android.view.View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocal()

        requestWindow()
        val layoutView = getLayoutActivity()
        mBinding = DataBindingUtil.setContentView(this, layoutView)
        Log.d(TAG, "onCreate: name Class: ${this::class.java.simpleName}")
        ITGTrackingHelper.addScreenTrack(this::class.java.simpleName)
        if (intent.getStringExtra(AppConstants.KEY_TRACKING_SCREEN_FROM) != null) {
            Routes.addTrackingMoveScreen(
                intent.getStringExtra(AppConstants.KEY_TRACKING_SCREEN_FROM).toString(),
                this::class.java.simpleName
            )
        }
        mBinding.lifecycleOwner = this
        registerBackPress()

        initLoadingView()
        initViews()
        onResizeViews()
        onClickViews()
        observerData()
    }

    open fun setUpViews() {}

    abstract fun getLayoutActivity(): Int

    open fun requestWindow() {}

    open fun initViews() {}

    open fun onResizeViews() {}

    open fun onClickViews() {}

    open fun observerData() {}

    private fun setLocal() {
        val languageCode = appSharedPref.languageCode
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onResume() {
        super.onResume()
        setUpSystemBars()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setUpSystemBars()
    }

    private fun registerBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onActivityBackPressed()
            }
        })
    }

    open fun onActivityBackPressed() {
        finish()
    }

    private var lastBackPressedTime = 0L

    protected fun handleDoubleBackToExit(timeWindowMs: Long = 2000) {
        if (lastBackPressedTime + timeWindowMs > System.currentTimeMillis()) {
            finish()
        } else {
            lastBackPressedTime = System.currentTimeMillis()
            Toast.makeText(this, R.string.press_back_again_to_exit, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Dialogs, ads and the keyboard can bring the bars back; hide them again on return.
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, mBinding.root).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            if (!shouldShowNavigationBars) hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setUpSystemBars() {
        hideSystemBars()
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, insets ->
            val bottom = if (shouldShowNavigationBars) {
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            } else 0
            // Status bar is hidden app-wide; layouts keep the design's 32dp top area (former status bar)
            // as their own top padding/margin, so the top padding from XML is left untouched here.
            view.updatePadding(bottom = bottom)
            insets
        }
    }

    protected fun showInterNotCheckGap(interAd: ApInterstitialAd?, func: (() -> Unit)) {
        if (interAd != null) {
            Admob.getInstance()
                .forceShowInterstitial(this, interAd.interstitialAd, object : AdCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        func.invoke()
                    }
                })
        } else func.invoke()
    }

    protected fun showLoading() {
        if (loadingView == null) {
            initLoadingView()
        }
        loadingView?.isVisible = true
    }

    protected fun hideLoading() {
        loadingView?.isVisible = false
    }

    private fun initLoadingView() {
        val parent: ViewGroup = findViewById(android.R.id.content)
        if (loadingView == null) {
            loadingView = LayoutInflater.from(this).inflate(R.layout.view_loading, parent, false)
            loadingView?.isVisible = false
            parent.addView(loadingView)
        }
    }
}
