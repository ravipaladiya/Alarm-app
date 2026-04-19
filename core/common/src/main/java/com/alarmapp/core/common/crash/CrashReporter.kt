package com.alarmapp.core.common.crash

/**
 * Platform-agnostic crash and non-fatal error sink.
 *
 * A no-op / log-only implementation ships by default so the app compiles and
 * runs without a Firebase project. To wire up real remote reporting, provide
 * a `FirebaseCrashReporter` (or alternative) binding that overrides the default
 * in a separate module once `google-services.json` is configured.
 */
interface CrashReporter {
    fun recordException(throwable: Throwable, message: String? = null)
    fun log(message: String)
    fun setUserId(id: String?)
    fun setCustomKey(key: String, value: String)
}
