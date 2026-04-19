package com.alarmapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alarmapp.feature.alarms.AlarmEditScreen
import com.alarmapp.feature.alarms.AlarmListScreen
import com.alarmapp.feature.permissions.PermissionsScreen
import com.alarmapp.feature.settings.SettingsScreen
import com.alarmapp.feature.sleep.SleepScreen

object Routes {
    const val ALARM_LIST = "alarms"
    const val ALARM_EDIT = "alarms/edit"
    const val ALARM_EDIT_PATTERN = "alarms/edit?id={id}"
    const val SETTINGS = "settings"
    const val SLEEP = "sleep"
    const val PERMISSIONS = "permissions"

    fun editAlarm(id: Long?): String =
        if (id == null) "alarms/edit" else "alarms/edit?id=$id"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.ALARM_LIST,
    ) {
        composable(Routes.ALARM_LIST) {
            AlarmListScreen(
                onAddAlarm = { navController.navigate(Routes.editAlarm(null)) },
                onEditAlarm = { id -> navController.navigate(Routes.editAlarm(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenSleep = { navController.navigate(Routes.SLEEP) },
                onOpenPermissions = { navController.navigate(Routes.PERMISSIONS) },
            )
        }
        composable(
            route = Routes.ALARM_EDIT_PATTERN,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            AlarmEditScreen(onDone = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPermissions = { navController.navigate(Routes.PERMISSIONS) },
            )
        }
        composable(Routes.SLEEP) {
            SleepScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onBack = { navController.popBackStack() })
        }
    }
}
