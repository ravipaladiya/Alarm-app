package com.alarmapp.core.common.crash

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [CrashReporter] that only writes to logcat via Timber. Useful in debug
 * builds and as a zero-config fallback before Firebase Crashlytics (or similar)
 * is wired in.
 */
@Singleton
class LoggingCrashReporter @Inject constructor() : CrashReporter {
    override fun recordException(throwable: Throwable, message: String?) {
        Timber.e(throwable, message ?: "Non-fatal")
    }

    override fun log(message: String) {
        Timber.i(message)
    }

    override fun setUserId(id: String?) {
        Timber.d("Crash reporter user id set: %s", id)
    }

    override fun setCustomKey(key: String, value: String) {
        Timber.d("Crash reporter %s=%s", key, value)
    }
}
