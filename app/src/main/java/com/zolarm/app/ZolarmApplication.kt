package com.zolarm.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.zolarm.app.core.NotificationChannels
import java.util.Locale

class ZolarmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applyArabicLocale(this)
        NotificationChannels.ensureAll(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(wrapArabic(base))
    }

    companion object {
        fun applyArabicLocale(context: Context) {
            val locale = Locale("ar")
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(config)
            }
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }

        fun wrapArabic(context: Context): Context {
            val locale = Locale("ar")
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                context
            }
        }
    }
}
