package com.zolarm.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.zolarm.app.core.LocaleHelper
import com.zolarm.app.core.NotificationChannels

class ZolarmApplication : MultiDexApplication() {

    override fun attachBaseContext(base: Context) {
        // MultiDex MUST run on the original Application path first
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        // Safe locale force (does not break Application context)
        LocaleHelper.applyAppLocale()
        NotificationChannels.ensureAll(this)
    }
}
