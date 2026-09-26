package com.zolarm.app

import android.app.Application
import com.zolarm.app.core.NotificationChannels

class ZolarmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureAll(this)
    }
}
