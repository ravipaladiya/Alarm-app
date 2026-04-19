package com.alarmapp.core.data.db

import androidx.room.migration.Migration

/**
 * Registered Room migrations. Every time the @Database version is bumped we must
 * add a [Migration] entry here; otherwise opening the DB against an existing
 * installation will throw [IllegalStateException]. We deliberately do NOT call
 * `fallbackToDestructiveMigration` in production because that wipes the user's
 * alarms.
 *
 * When bumping the schema version:
 *  1. Update `@Database(version = N)` in [AlarmDatabase].
 *  2. Run the build so Room regenerates the exported schema JSON under
 *     `core/data/schemas/com.alarmapp.core.data.db.AlarmDatabase/`.
 *  3. Commit both the new schema file and a new `Migration` below.
 *  4. Add a `MigrationTestHelper` test in `src/androidTest` that migrates
 *     from N-1 to N and asserts the data survived.
 */
object AlarmDatabaseMigrations {
    val ALL: Array<Migration> = emptyArray()
}
