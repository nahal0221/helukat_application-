package com.yourcompany.fieldtech.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yourcompany.fieldtech.data.local.dao.SyncDao
import com.yourcompany.fieldtech.data.local.entity.*

@Database(
    entities = [
        TimeLogEntity::class,
        StatusUpdateEntity::class,
        MaterialUsageEntity::class,
        PhotoEntity::class,
        JobCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncDao(): SyncDao
}
