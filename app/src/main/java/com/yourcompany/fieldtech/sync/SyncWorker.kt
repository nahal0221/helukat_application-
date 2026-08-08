package com.yourcompany.fieldtech.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourcompany.fieldtech.data.local.dao.SyncDao
import com.yourcompany.fieldtech.data.remote.ApiService
import com.yourcompany.fieldtech.data.remote.dto.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID

/**
 * Flushes all unsynced local records to POST /sync/batch in one call, then
 * reconciles server-assigned IDs back onto the local rows using the
 * local_id -> server_id mapping the endpoint returns.
 *
 * Runs: on reconnect (via a NetworkType.CONNECTED constraint where this is
 * enqueued) and periodically as a safety net. Idempotency-Key ensures a
 * retried request on a flaky connection never double-creates records
 * server-side, per the spec's Design Notes.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: ApiService,
    private val dao: SyncDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val timeLogs = dao.getUnsyncedTimeLogs()
        val statusUpdates = dao.getUnsyncedStatusUpdates()
        val materialUsage = dao.getUnsyncedMaterialUsage()
        val photos = dao.getUnsyncedPhotos()

        if (timeLogs.isEmpty() && statusUpdates.isEmpty() && materialUsage.isEmpty() && photos.isEmpty()) {
            return Result.success()
        }

        val batch = SyncBatchRequest(
            timeLogs = timeLogs.map {
                TimeLogPayload(it.localId, it.jobId, it.eventType, it.eventTime, it.latitude, it.longitude)
            },
            statusUpdates = statusUpdates.map {
                StatusUpdatePayload(it.localId, it.jobId, it.statusNote, it.capturedAt)
            },
            materialUsage = materialUsage.map {
                MaterialUsagePayload(it.localId, it.jobId, it.materialId, it.quantityUsed, it.capturedAt)
            },
            photos = photos.map {
                PhotoPayload(it.localId, it.jobId, it.caption, it.capturedAt)
            }
        )

        // A fresh key per attempt at the *outer* retry level would defeat idempotency;
        // this key is derived from the batch's content set so identical retries reuse it.
        val idempotencyKey = idempotencyKeyFor(timeLogs.map { it.localId } + statusUpdates.map { it.localId } +
            materialUsage.map { it.localId } + photos.map { it.localId })

        return try {
            val response = api.syncBatch(idempotencyKey, batch)
            response.timeLogs.forEach { dao.markTimeLogSynced(it.localId, it.serverId) }
            response.statusUpdates.forEach { dao.markStatusUpdateSynced(it.localId, it.serverId) }
            response.materialUsage.forEach { dao.markMaterialUsageSynced(it.localId, it.serverId) }
            response.photos.forEach { dao.markPhotoSynced(it.localId, it.serverId) }
            Result.success()
        } catch (e: Exception) {
            // Network hiccup or 5xx: let WorkManager's backoff policy retry.
            // A 4xx here would indicate a client bug and shouldn't retry forever in production.
            Result.retry()
        }
    }

    private fun idempotencyKeyFor(localIds: List<String>): String {
        // Stable across retries of the *same* pending set; changes once new
        // items are queued, which is the desired behavior.
        return UUID.nameUUIDFromBytes(localIds.sorted().joinToString(",").toByteArray()).toString()
    }
}
