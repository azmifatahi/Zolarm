package com.zolarm.app

import android.app.Application
import android.content.Context
import com.zolarm.app.core.LocaleHelper
import com.zolarm.app.core.NotificationChannels

class ZolarmApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.applyAppLocale()
        NotificationChannels.ensureAll(this)
    }
}
