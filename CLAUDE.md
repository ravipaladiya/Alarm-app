# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project summary

Android alarm app written in Kotlin, Jetpack Compose, Material 3. Multi-module
Gradle build with Hilt DI, Room persistence, and `AlarmManager` scheduling.
Minimum SDK is **26 (Android 8.0)**; target SDK is **35**. JDK **17** is required.

## Build & test commands

```bash
./gradlew :app:assembleDebug                       # Debug APK
./gradlew :app:installDebug                        # Install on connected device
./gradlew check                                    # Unit tests + lint (all modules)
./gradlew :feature:alarms:testDebugUnitTest        # Tests for one module
./gradlew :feature:alarms:testDebugUnitTest \
    --tests "*AlarmEditViewModelTest.save_invokes_*"   # Single test
./gradlew spotlessApply                            # Format Kotlin + Gradle scripts
./gradlew detekt                                   # Static analysis
./gradlew connectedDebugAndroidTest                # Instrumentation tests (needs emulator/device)
```

Gradle convention plugins live under `build-logic/` and are applied via
`id("alarmapp.android.<name>")`. Versions are centralized in
`gradle/libs.versions.toml`.

## Module graph

| Module | Purpose |
|---|---|
| `:app` | `Application`, `MainActivity`, `NavHost`, `AndroidManifest` with all alarm permissions. |
| `:core:common` | Coroutine dispatcher qualifiers, `Clock`, DST-safe `nextTriggerEpochMillis`. |
| `:core:designsystem` | Material 3 theme + dynamic color, shared Composables. |
| `:core:domain` | Pure Kotlin (JVM) — `Alarm`, `DismissChallenge`, repository interfaces, use cases, `AlarmScheduler` contract. No Android deps. |
| `:core:data` | Room (`AlarmDatabase`, DAOs), DataStore settings, repository implementations, mappers. |
| `:core:alarm` | `AlarmManagerScheduler`, `AlarmBroadcastReceiver`, `BootReceiver`, `TimeChangedReceiver`, `AlarmService`, `RingtonePlayer`, `VibrationPlayer`, `NotificationChannels`. |
| `:feature:alarms` | Alarm list + edit screens + ViewModels. |
| `:feature:ringing` | `RingingActivity` (showWhenLocked/turnScreenOn) and `RingingScreen`. |
| `:feature:challenges` | `ChallengeHost` plus Math, Shake, Typing, QR handlers. |
| `:feature:settings` | Theme, dynamic color, cloud-sync toggle, default snooze. |
| `:feature:sleep` | Bedtime/sleep screen (stub). |

## Alarm correctness rules (read before changing `:core:alarm`)

These constraints are invisible from any single file but are the reason the app
fires reliably. **Do not regress them.**

1. **Schedule with `AlarmManager.setAlarmClock(...)`** — the only API exempt
   from Doze and App Standby on API 26+. Never substitute `setExact`,
   `setExactAndAllowWhileIdle`, or WorkManager for alarm triggers. See
   `AlarmManagerScheduler.schedule` in `:core:alarm`.
2. **Permissions:** `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`,
   `USE_FULL_SCREEN_INTENT`, `POST_NOTIFICATIONS`,
   `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `FOREGROUND_SERVICE_SPECIAL_USE`,
   `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`. All declared in
   `app/src/main/AndroidManifest.xml`.
3. **Full-screen notification** on the `alarm_ringing` notification channel
   launches `RingingActivity`. The channel itself is silent — audio is driven
   by `RingtonePlayer` so we can fade and honor per-alarm selection.
4. **Foreground service** `AlarmService` holds the ringing state using
   `FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK | SPECIAL_USE` on API 34+.
5. **Boot + package-replace:** `BootReceiver` handles `BOOT_COMPLETED`,
   `LOCKED_BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, and `PACKAGE_REPLACED` by
   calling `AlarmScheduler.rescheduleAll(repository.allEnabled())`.
   `android:directBootAware="true"` so this works before first unlock.
6. **Time zone / DST / locale:** `TimeChangedReceiver` upserts every enabled
   alarm on `TIMEZONE_CHANGED`, `TIME_SET`, or `LOCALE_CHANGED`, which causes
   `AlarmRepositoryImpl.upsert` to recompute `nextTriggerEpochMs` via
   `nextTriggerEpochMillis` using the current local clock.
7. **Wake lock:** `AlarmBroadcastReceiver` grabs a 30-second
   `PARTIAL_WAKE_LOCK` to bridge from broadcast delivery to `AlarmService`
   promoting itself to the foreground. Release must happen in `finally`.
8. **MediaPlayer audio attributes:** always `USAGE_ALARM` — not
   `USAGE_MEDIA` or `USAGE_NOTIFICATION`. This routes through the alarm
   stream and bypasses Do Not Disturb when the channel has
   `setBypassDnd(true)`.
9. **`AlarmClockInfo.showIntent`** points to `MainActivity`, not the ringing
   activity — it's the "edit this alarm" deep link the system shows in the
   status-bar clock menu.

## Data flow

```
ViewModel  →  UseCase  →  AlarmRepository (interface, :core:domain)
                                ↓
                        AlarmRepositoryImpl (:core:data)
                                ↓
                        AlarmDao (Room, :core:data)
```

When saving an alarm, `SaveAlarmUseCase` writes to Room and then calls
`AlarmScheduler.schedule` / `cancel`. The scheduler interface is in
`:core:domain` and implemented by `AlarmManagerScheduler` in `:core:alarm`.
The ViewModel layer never talks to `AlarmManager` directly.

`Alarm` stores local `hour`/`minute` + `repeatDaysMask` (bit 0 = Monday). The
absolute `nextTriggerEpochMs` is computed on every upsert by
`core.common.time.nextTriggerEpochMillis`, which resolves DST via
`java.time.ZonedDateTime` and walks up to 7 days forward to find the next
enabled weekday.

## Adding a new dismiss challenge

1. Add a `data class` to `DismissChallenge` in
   `core/domain/src/main/kotlin/com/alarmapp/core/domain/model/Alarm.kt`. The
   sealed interface is `@Serializable`, so kotlinx-serialization handles
   round-tripping through the `dismissChallengeJson` Room column.
2. Add a handler `@Composable` in `:feature:challenges` and wire a new branch
   in `ChallengeHost`.
3. Add a `FilterChip` entry in the challenge row of
   `feature/alarms/src/main/java/com/alarmapp/feature/alarms/AlarmEditScreen.kt`.
4. No database migration is needed — the challenge payload lives inside the
   existing JSON column.

## Sync strategy (once Firebase lands)

- Auth: Firebase Auth (Google + anonymous fallback).
- Store: Firestore path `/users/{uid}/alarms/{remoteId}`.
- Conflict resolution: last-write-wins on `updatedAtEpochMs`; Room is the
  source of truth when offline.
- Triggers: `OneTimeWorkRequest` on local change; `PeriodicWorkRequest` every
  6 hours as a safety net. **Do not** sync trigger times from remote —
  always recompute via `nextTriggerEpochMillis` locally, because ringtone
  URIs and exact alarm permissions are device-scoped.

## Testing conventions

- **Pure Kotlin logic** (`nextTriggerEpochMillis`, use cases, mappers) is
  tested in plain JUnit under `src/test` with a `FixedClock` pattern — see
  `core/common/src/test/java/com/alarmapp/core/common/time/NextTriggerTest.kt`.
- **ViewModels** use `StandardTestDispatcher` + `Turbine` + `MockK` — see
  `feature/alarms/src/test/java/com/alarmapp/feature/alarms/AlarmEditViewModelTest.kt`.
- **Scheduler integration** should be tested with Robolectric (`ShadowAlarmManager`)
  for weekday/DST coverage — not yet written; add here when adding new scheduling logic.
- Hilt-instrumented tests use the custom `HiltTestRunner` in
  `app/src/androidTest/java/com/alarmapp/HiltTestRunner.kt`.

## Code style

- ktlint 1.3.1 via Spotless. Run `./gradlew spotlessApply` before committing.
- Detekt with `config/detekt/detekt.yml` — max line length 140.
- No `BuildConfig` by default (`android.defaults.buildfeatures.buildconfig=false`);
  check `ApplicationInfo.FLAG_DEBUGGABLE` if you need a debug flag.
- Compose screens are stateless; all state lives in `HiltViewModel`s exposed
  as `StateFlow`.
- Inject dispatchers via the qualifiers in `core.common` — never reference
  `Dispatchers.IO` directly from production code.
- Prefer `@Binds` over `@Provides` where possible (see the `Module` files in
  `:core:data` and `:core:alarm`).

## Gotchas

- **OEM battery killers** (Xiaomi, Oppo, OnePlus in aggressive mode) can drop
  `setAlarmClock` despite the Doze exemption. Tell users to allowlist the app
  — don't try to hide the issue.
- **Gradle wrapper jar** (`gradle/wrapper/gradle-wrapper.jar`) is a binary
  that this repo does not commit as source. Run `gradle wrapper
  --gradle-version 8.10.2` (using an installed Gradle or Android Studio) to
  regenerate it after cloning.
- **`google-services.json`** is git-ignored. Cloud-sync builds require a
  Firebase project; until one is set up, leave `cloudSyncEnabled` off.
- **Do not** write code that references `BuildConfig` unless you first add
  `buildFeatures.buildConfig = true` to the relevant `android` block.
