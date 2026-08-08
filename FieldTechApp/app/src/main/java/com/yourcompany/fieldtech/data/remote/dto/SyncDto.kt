package com.yourcompany.fieldtech.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Individual write payloads (also embedded in SyncBatchRequest) ---

@JsonClass(generateAdapter = true)
data class TimeLogPayload(
    @Json(name = "local_id") val localId: String,
    @Json(name = "job_id") val jobId: Long,
    @Json(name = "event_type") val eventType: String, // arrival | departure | break_start | break_end
    @Json(name = "event_time") val eventTime: String, // ISO-8601, client-captured
    val latitude: Double,
    val longitude: Double
)

@JsonClass(generateAdapter = true)
data class StatusUpdatePayload(
    @Json(name = "local_id") val localId: String,
    @Json(name = "job_id") val jobId: Long,
    @Json(name = "status_note") val statusNote: String,
    @Json(name = "captured_at") val capturedAt: String
)

@JsonClass(generateAdapter = true)
data class MaterialUsagePayload(
    @Json(name = "local_id") val localId: String,
    @Json(name = "job_id") val jobId: Long,
    @Json(name = "material_id") val materialId: Long,
    @Json(name = "quantity_used") val quantityUsed: Double,
    @Json(name = "captured_at") val capturedAt: String
)

@JsonClass(generateAdapter = true)
data class PhotoPayload(
    @Json(name = "local_id") val localId: String,
    @Json(name = "job_id") val jobId: Long,
    val caption: String? = null,
    @Json(name = "captured_at") val capturedAt: String
    // Actual image bytes go via multipart on POST /jobs/{id}/photos, not in the batch body.
)

// --- POST /sync/batch request/response ---

@JsonClass(generateAdapter = true)
data class SyncBatchRequest(
    @Json(name = "time_logs") val timeLogs: List<TimeLogPayload> = emptyList(),
    @Json(name = "status_updates") val statusUpdates: List<StatusUpdatePayload> = emptyList(),
    @Json(name = "material_usage") val materialUsage: List<MaterialUsagePayload> = emptyList(),
    val photos: List<PhotoPayload> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SyncIdMapping(
    @Json(name = "local_id") val localId: String,
    @Json(name = "server_id") val serverId: Long
)

@JsonClass(generateAdapter = true)
data class SyncBatchResponse(
    @Json(name = "time_logs") val timeLogs: List<SyncIdMapping> = emptyList(),
    @Json(name = "status_updates") val statusUpdates: List<SyncIdMapping> = emptyList(),
    @Json(name = "material_usage") val materialUsage: List<SyncIdMapping> = emptyList(),
    val photos: List<SyncIdMapping> = emptyList()
)
