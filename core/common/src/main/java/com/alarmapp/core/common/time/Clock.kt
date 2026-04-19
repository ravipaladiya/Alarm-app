package com.alarmapp.core.common.time

import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface Clock {
    fun nowMillis(): Long
    fun zone(): ZoneId
}

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
