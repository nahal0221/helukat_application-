package com.yourcompany.fieldtech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Every offline-write entity shares this shape:
 *  - localId:   client-generated UUID, used to reconcile with server_id after sync
 *  - synced:    false until /sync/batch confirms it
 *  - serverId:  populated from the SyncBatchResponse local_id -> server_id mapping
 *  - capturedAt: the true event time, preserved even if the sync happens later
 *    (this is what the spec calls "offline-tolerant" in §5)
 */

@Entity(tableName = "time_log_queue")
data class TimeLogEntity(
    @PrimaryKey val localId: String,
    val jobId: Long,
    val eventType: String,
    val eventTime: String,
    val latitude: Double,
    val longitude: Double,
    val synced: Boolean = false,
    val serverId: Long? = null
)

@Entity(tableName = "status_update_queue")
data class StatusUpdateEntity(
    @PrimaryKey val localId: String,
    val jobId: Long,
    val statusNote: String,
    val capturedAt: String,
    val synced: Boolean = false,
    val serverId: Long? = null
)

@Entity(tableName = "material_usage_queue")
data class MaterialUsageEntity(
    @PrimaryKey val localId: String,
    val jobId: Long,
    val materialId: Long,
    val quantityUsed: Double,
    val capturedAt: String,
    val synced: Boolean = false,
    val serverId: Long? = null
)

@Entity(tableName = "photo_queue")
data class PhotoEntity(
    @PrimaryKey val localId: String,
    val jobId: Long,
    val localFilePath: String,
    val caption: String?,
    val capturedAt: String,
    val synced: Boolean = false,
    val serverId: Long? = null
)

/** Read-through cache of the technician's assigned jobs, for offline viewing. */
@Entity(tableName = "job_cache")
data class JobCacheEntity(
    @PrimaryKey val jobId: Long,
    val siteId: Long,
    val status: String,
    val priority: String?,
    val scheduledAt: String?,
    val siteName: String?,
    val clientName: String?
)
