package com.alarmapp.core.common.crash

import android.util.Log
import timber.log.Timber

/**
 * Timber tree that forwards non-debug log entries to the configured
 * [CrashReporter]. Install alongside (not instead of) [Timber.DebugTree] in
 * release builds so warnings and errors show up in the crash report backend.
 */
class CrashReportingTree(private val reporter: CrashReporter) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return
        reporter.log(if (tag != null) "[$tag] $message" else message)
        if (t != null && (priority == Log.ERROR || priority == Log.ASSERT)) {
            reporter.recordException(t, message)
        }
    }
}
