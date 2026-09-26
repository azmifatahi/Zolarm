package com.zolarm.app

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.zolarm.app.core.LocaleHelper
import com.zolarm.app.core.NotificationChannels

class ZolarmApplication : MultiDexApplication() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.applyAppLocale()
        NotificationChannels.ensureAll(this)
    }
}
