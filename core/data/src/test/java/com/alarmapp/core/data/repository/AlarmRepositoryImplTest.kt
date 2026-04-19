package com.alarmapp.core.data.repository

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import com.alarmapp.core.common.time.Clock
import com.alarmapp.core.data.db.AlarmDatabase
import com.alarmapp.core.domain.model.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AlarmRepositoryImplTest {

    private lateinit var db: AlarmDatabase
    private lateinit var repo: AlarmRepositoryImpl

    private val fixedClock = object : Clock {
        override fun nowMillis(): Long =
            LocalDateTime.of(2026, 4, 19, 6, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        override fun zone(): ZoneId = ZoneOffset.UTC
    }

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AlarmDatabase::class.java,
        ).allowMainThreadQueries().build()
        repo = AlarmRepositoryImpl(db.alarmDao(), fixedClock, Dispatchers.Unconfined)
    }

    @After fun tearDown() {
        db.close()
    }

    @Test fun `upsert computes next trigger time based on clock`() = runTest {
        val id = repo.upsert(Alarm(hour = 7, minute = 30))
        val loaded = repo.getById(id)
        assertNotNull(loaded)
        val expected = LocalDateTime.of(2026, 4, 19, 7, 30).toInstant(ZoneOffset.UTC).toEpochMilli()
        assertEquals(expected, loaded!!.nextTriggerEpochMs)
    }

    @Test fun `upsert stamps updatedAtEpochMs to clock`() = runTest {
        val id = repo.upsert(Alarm(hour = 7, minute = 30))
        val loaded = repo.getById(id)
        assertEquals(fixedClock.nowMillis(), loaded!!.updatedAtEpochMs)
    }

    @Test fun `setEnabled updates the flag without changing the canonical time`() = runTest {
        val id = repo.upsert(Alarm(hour = 7, minute = 30, enabled = true))
        val before = repo.getById(id)!!
        repo.setEnabled(id, enabled = false)
        val after = repo.getById(id)!!
        assertEquals(false, after.enabled)
        assertEquals(before.hour, after.hour)
        assertEquals(before.minute, after.minute)
    }

    @Test fun `allEnabled returns only enabled rows`() = runTest {
        repo.upsert(Alarm(hour = 6, minute = 0, enabled = true, label = "keep"))
        val disabledId = repo.upsert(Alarm(hour = 7, minute = 0, enabled = true, label = "drop"))
        repo.setEnabled(disabledId, enabled = false)

        val enabled = repo.allEnabled()
        assertEquals(1, enabled.size)
        assertEquals("keep", enabled.first().label)
    }

    @Test fun `delete removes the row`() = runTest {
        val id = repo.upsert(Alarm(hour = 6, minute = 0))
        assertNotNull(repo.getById(id))
        repo.delete(id)
        assertEquals(null, repo.getById(id))
    }

    @Test fun `upsert with existing id updates rather than inserting`() = runTest {
        val id = repo.upsert(Alarm(hour = 6, minute = 0, label = "first"))
        repo.upsert(Alarm(id = id, hour = 6, minute = 30, label = "updated"))
        val loaded = repo.getById(id)!!
        assertEquals("updated", loaded.label)
        assertEquals(30, loaded.minute)
        assertNotEquals(0L, loaded.nextTriggerEpochMs)
    }
}
