package com.yourcompany.fieldtech.data.local.dao

import androidx.room.*
import com.yourcompany.fieldtech.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {

    // --- Inserts (called immediately when the tech performs an action, online or not) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeLog(entity: TimeLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusUpdate(entity: StatusUpdateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterialUsage(entity: MaterialUsageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(entity: PhotoEntity)

    // --- Unsynced reads (what SyncWorker batches up) ---
    @Query("SELECT * FROM time_log_queue WHERE synced = 0")
    suspend fun getUnsyncedTimeLogs(): List<TimeLogEntity>

    @Query("SELECT * FROM status_update_queue WHERE synced = 0")
    suspend fun getUnsyncedStatusUpdates(): List<StatusUpdateEntity>

    @Query("SELECT * FROM material_usage_queue WHERE synced = 0")
    suspend fun getUnsyncedMaterialUsage(): List<MaterialUsageEntity>

    @Query("SELECT * FROM photo_queue WHERE synced = 0")
    suspend fun getUnsyncedPhotos(): List<PhotoEntity>

    // --- Mark synced after the server echoes back local_id -> server_id ---
    @Query("UPDATE time_log_queue SET synced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markTimeLogSynced(localId: String, serverId: Long)

    @Query("UPDATE status_update_queue SET synced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markStatusUpdateSynced(localId: String, serverId: Long)

    @Query("UPDATE material_usage_queue SET synced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markMaterialUsageSynced(localId: String, serverId: Long)

    @Query("UPDATE photo_queue SET synced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markPhotoSynced(localId: String, serverId: Long)

    // --- Job cache for offline viewing ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheJobs(jobs: List<JobCacheEntity>)

    @Query("SELECT * FROM job_cache ORDER BY scheduledAt ASC")
    fun observeCachedJobs(): Flow<List<JobCacheEntity>>

    @Query("""
        SELECT
            (SELECT COUNT(*) FROM time_log_queue WHERE synced = 0) +
            (SELECT COUNT(*) FROM status_update_queue WHERE synced = 0) +
            (SELECT COUNT(*) FROM material_usage_queue WHERE synced = 0) +
            (SELECT COUNT(*) FROM photo_queue WHERE synced = 0)
    """)
    fun observePendingSyncCount(): Flow<Int>
}
