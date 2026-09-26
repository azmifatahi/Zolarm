package com.zolarm.app.ui.overlay

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.zolarm.app.core.AlarmExtras
import com.zolarm.app.core.AlarmPayload
import com.zolarm.app.service.ZolarmForegroundService
import com.zolarm.app.ui.screens.ZolarmOverlayScreen
import com.zolarm.app.ui.theme.Ink900
import com.zolarm.app.ui.theme.ZolarmTheme

class ZolarmOverlayActivity : ComponentActivity() {

    private lateinit var payload: AlarmPayload

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        payload = AlarmExtras.read(intent) ?: run { finish(); return }

        configureWindowForLockScreen()
        blockBackButton()

        setContent {
            ZolarmTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Ink900),
                    color = Color.Transparent
                ) {
                    ZolarmOverlayScreen(
                        payload = payload,
                        onDismissed = { finishAlarm() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        AlarmExtras.read(intent)?.let { payload = it }
    }

    private fun configureWindowForLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val keyguard = getSystemService(KeyguardManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && keyguard?.isKeyguardLocked == true) {
            keyguard.requestDismissKeyguard(this, null)
        }
    }

    private fun blockBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })
    }

    private fun finishAlarm() {
        ZolarmForegroundService.stop(applicationContext)
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        ZolarmForegroundService.stop(applicationContext)
    }
}
