package com.zolarm.app.core

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    private const val LANG = "ar"

    /** Call once at app start – persists for the process. */
    fun applyAppLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(LANG))
        Locale.setDefault(Locale(LANG))
    }

    /** Wrap Context so resources (strings, layouts) load Arabic. */
    fun wrap(context: Context): Context {
        val locale = Locale(LANG)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
