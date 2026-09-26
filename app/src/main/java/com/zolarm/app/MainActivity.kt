package com.zolarm.app

import android.Manifest
import android.os.Build
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zolarm.app.core.LocaleHelper
import com.zolarm.app.core.NotificationChannels
import com.zolarm.app.core.ZolarmPermissions
import com.zolarm.app.data.AlarmEntity
import com.zolarm.app.ui.screens.AddEditAlarmScreen
import com.zolarm.app.ui.screens.ZolarmDashboardScreen
import com.zolarm.app.ui.theme.ZolarmTheme
import com.zolarm.app.ui.viewmodel.AlarmViewModel

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    private val runtimePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* results surface via ZolarmPermissions */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        LocaleHelper.applyAppLocale()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationChannels.ensureAll(this)
        requestStartupPermissions()

        setContent {
            ZolarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { ZolarmApp() }
            }
        }
    }

    private fun requestStartupPermissions() {
        val toRequest = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ZolarmPermissions.hasRuntime(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
            ) add(Manifest.permission.POST_NOTIFICATIONS)

            if (!ZolarmPermissions.hasRuntime(this@MainActivity, Manifest.permission.CAMERA)) {
                add(Manifest.permission.CAMERA)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !ZolarmPermissions.hasRuntime(this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION)
            ) add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (toRequest.isNotEmpty()) runtimePermissionLauncher.launch(toRequest.toTypedArray())
    }
}

private const val ROUTE_DASHBOARD = "dashboard"
private const val ROUTE_ADD_EDIT = "add_edit"
private const val ARG_ALARM_ID = "alarmId"

@Composable
private fun ZolarmApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: AlarmViewModel = viewModel(factory = AlarmViewModel.factory(context))
    val alarms by viewModel.alarms.collectAsState()

    NavHost(navController = navController, startDestination = ROUTE_DASHBOARD) {

        composable(ROUTE_DASHBOARD) {
            ZolarmDashboardScreen(
                alarms = alarms,
                onToggleAlarm = viewModel::toggleAlarm,
                onEditAlarm = { alarm ->
                    navController.navigate("$ROUTE_ADD_EDIT?$ARG_ALARM_ID=${alarm.id}")
                },
                onDeleteAlarm = viewModel::deleteAlarm,
                onAddAlarm = { navController.navigate(ROUTE_ADD_EDIT) }
            )
        }

        composable(
            route = "$ROUTE_ADD_EDIT?$ARG_ALARM_ID={$ARG_ALARM_ID}",
            arguments = listOf(
                navArgument(ARG_ALARM_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val alarmId = entry.arguments?.getLong(ARG_ALARM_ID) ?: -1L
            val existing: AlarmEntity? = alarms.firstOrNull { it.id == alarmId }

            AddEditAlarmScreen(
                initialAlarm = existing,
                onSave = { alarm ->
                    viewModel.saveAlarm(alarm)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
