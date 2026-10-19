package com.itg.template.ui.component.uninstall

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.itg.template.R
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
                        rank = 2
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
        rank: Int
    ): ShortcutInfo = ShortcutInfo.Builder(context, id)
        .setShortLabel(context.getSystemLocaleString(label))
        .setLongLabel(context.getSystemLocaleString(label))
        .setIcon(Icon.createWithResource(context, icon))
        .setIntent(Intent(context, SplashActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = intentAction
            putExtra(AppConstants.FROM_SHORTCUT, id)
        })
        .setRank(rank)
        .build()
}
