# Alarm App

A production-oriented Android alarm app written in Kotlin with Jetpack Compose.

## Feature scope

| Bundle | Status |
|---|---|
| Core alarm UX (repeat, fade-in, snooze, auto-silence) | Scaffolded |
| Dismiss challenges (math, shake, typing, QR) | Math/shake/typing implemented; QR placeholder |
| Theming & labels (Material You dynamic color) | Scaffolded |
| Cloud sync + sleep tracking | Interfaces + stub screens; Firebase wiring pending |

## Requirements

- JDK 17
- Android Studio Koala (2024.1.1) or newer
- Android SDK platform 35 installed
- Minimum supported device: Android 8.0 (API 26)

## Getting started

1. Clone the repo.
2. Copy `local.properties.template` to `local.properties` and set `sdk.dir`.
3. `./gradlew :app:assembleDebug` to build the debug APK.
4. `./gradlew :app:installDebug` to install on a connected device/emulator.

## Useful commands

| Task | Command |
|---|---|
| Build debug APK | `./gradlew :app:assembleDebug` |
| Install debug | `./gradlew :app:installDebug` |
| All unit tests + lint | `./gradlew check` |
| Single module unit tests | `./gradlew :core:common:testDebugUnitTest` |
| Single test | `./gradlew :feature:alarms:testDebugUnitTest --tests "*AlarmEditViewModelTest.save_invokes_*"` |
| Format | `./gradlew spotlessApply` |
| Static analysis | `./gradlew detekt` |
| Connected instrumentation tests | `./gradlew connectedDebugAndroidTest` |

## License

See [LICENSE](LICENSE) once added. Copyright (c) 2026.
