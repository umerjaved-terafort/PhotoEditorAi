package com.itg.template.ui.component.uninstall

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.itg.template.R
import com.itg.template.ads.RemoteConfigUtils
import com.itg.template.app.AppConstants
import com.itg.template.ui.bases.ext.getSystemLocaleString
import com.itg.template.ui.component.splash.SplashActivity

/**
 * Launcher long-press quick actions. Order (top -> bottom, closest to the app icon first):
 * AI Templates, Photo Enhancer, Uninstall. The popup itself (header, app info, widgets row)
 * is drawn by the launcher and cannot be styled from the app.
 */
object ShortcutManager {
    private const val ACTION_OPEN_AI_TEMPLATES = "ACTION_OPEN_AI_TEMPLATES"
    private const val ACTION_OPEN_PHOTO_ENHANCER = "ACTION_OPEN_PHOTO_ENHANCER"

    /**
     * Adds the quick actions (incl. Uninstall) when the remote flag allows it, otherwise removes them.
     * Called on every launch once Remote Config is ready, so labels follow the system locale.
     */
    fun applyFromRemoteConfig(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return
        // on_enable_uninstall_widget is an on/off switch for every user. Passing it to ERain's
        // getShouldDisplayWidgetUninstall() treated it as a UA-only check and hid the shortcut for
        // organic users (QA Ai Photo_12).
        if (RemoteConfigUtils.getOnEnableUninstallWidget()) {
            initShortCut(context)
        } else {
            try {
                context.getSystemService(ShortcutManager::class.java)?.removeAllDynamicShortcuts()
            } catch (_: Exception) {
            }
        }
    }

    fun initShortCut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val manager = context.getSystemService(ShortcutManager::class.java) ?: return
            try {
                manager.removeAllDynamicShortcuts()
                val shortcuts = listOf(
                    buildShortcut(
                        context,
                        id = ACTION_OPEN_AI_TEMPLATES,
                        label = R.string.shortcut_ai_templates,
                        icon = R.drawable.ic_shortcut_ai_templates,
                        intentAction = "android.intent.action.SHORTCUT_AI_TEMPLATES",
                        rank = 0
                    ),
                    buildShortcut(
                        context,
                        id = ACTION_OPEN_PHOTO_ENHANCER,
                        label = R.string.shortcut_photo_enhancer,
                        icon = R.drawable.ic_shortcut_photo_enhancer,
                        intentAction = "android.intent.action.SHORTCUT_PHOTO_ENHANCER",
                        rank = 1
                    ),
                    buildShortcut(
                        context,
                        id = AppConstants.ACTION_OPEN_UNINSTALL,
                        label = R.string.txt_uninstall,
                        icon = R.drawable.ic_shortcut_uninstall,
                        intentAction = "android.intent.action.SHORTCUT_UNINSTALL_APP",
                        rank = 2,
                        // Uninstall opens the Confirm Uninstall screen directly (no Splash, consent or splash ads).
                        target = ConfirmUninstallActivity::class.java
                    )
                )
                // Respect the launcher limit (usually 4-5 activity shortcuts).
                manager.dynamicShortcuts = shortcuts.take(manager.maxShortcutCountPerActivity)
            } catch (_: Exception) {
            }
        }
    }

    private fun buildShortcut(
        context: Context,
        id: String,
        @StringRes label: Int,
        @DrawableRes icon: Int,
        intentAction: String,
        rank: Int,
        target: Class<out Activity> = SplashActivity::class.java
    ): ShortcutInfo = ShortcutInfo.Builder(context, id)
        .setShortLabel(context.getSystemLocaleString(label))
        .setLongLabel(context.getSystemLocaleString(label))
        .setIcon(Icon.createWithResource(context, icon))
        .setIntent(Intent(context, target).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = intentAction
            putExtra(AppConstants.FROM_SHORTCUT, id)
        })
        .setRank(rank)
        .build()
}
