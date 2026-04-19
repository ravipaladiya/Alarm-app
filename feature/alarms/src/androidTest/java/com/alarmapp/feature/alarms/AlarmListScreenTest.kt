package com.alarmapp.feature.alarms

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

/**
 * Smoke test for the [AlarmListScreen] empty state. Exercised via the
 * `internal` [EmptyState] composable so we don't need Hilt wiring in the
 * instrumentation test.
 *
 * Execute with:
 *   ./gradlew :feature:alarms:connectedDebugAndroidTest
 *
 * Requires a connected device or running emulator.
 */
class AlarmListScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test fun empty_state_shows_title_and_add_button() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            EmptyState(onAddAlarm = {})
        }
        composeRule.onNodeWithText(context.getString(R.string.alarm_list_empty_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.alarm_list_add))
            .assertIsDisplayed()
    }
}
