package com.yourcompany.fieldtech.data.repository

import com.yourcompany.fieldtech.data.local.dao.SyncDao
import com.yourcompany.fieldtech.data.local.entity.MaterialUsageEntity
import com.yourcompany.fieldtech.data.local.entity.StatusUpdateEntity
import com.yourcompany.fieldtech.data.local.entity.TimeLogEntity
import com.yourcompany.fieldtech.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Every write here follows the same pattern: capture the timestamp locally,
 * insert into the Room queue immediately (so the UI updates instantly and
 * nothing is lost if the app is offline), then kick the sync worker which
 * flushes to POST /sync/batch when connected.
 */
class FieldActivityRepository @Inject constructor(
    private val dao: SyncDao,
    private val syncScheduler: SyncScheduler
) {
    fun observePendingSyncCount(): Flow<Int> = dao.observePendingSyncCount()

    suspend fun logTimeEvent(jobId: Long, eventType: String, latitude: Double, longitude: Double) {
        dao.insertTimeLog(
            TimeLogEntity(
                localId = UUID.randomUUID().toString(),
                jobId = jobId,
                eventType = eventType,
                eventTime = Instant.now().toString(),
                latitude = latitude,
                longitude = longitude
            )
        )
        syncScheduler.requestImmediateSync()
    }

    suspend fun postStatusNote(jobId: Long, note: String) {
        dao.insertStatusUpdate(
            StatusUpdateEntity(
                localId = UUID.randomUUID().toString(),
                jobId = jobId,
                statusNote = note,
                capturedAt = Instant.now().toString()
            )
        )
        syncScheduler.requestImmediateSync()
    }

    suspend fun logMaterialUsage(jobId: Long, materialId: Long, quantityUsed: Double) {
        dao.insertMaterialUsage(
            MaterialUsageEntity(
                localId = UUID.randomUUID().toString(),
                jobId = jobId,
                materialId = materialId,
                quantityUsed = quantityUsed,
                capturedAt = Instant.now().toString()
            )
        )
        syncScheduler.requestImmediateSync()
    }
}
