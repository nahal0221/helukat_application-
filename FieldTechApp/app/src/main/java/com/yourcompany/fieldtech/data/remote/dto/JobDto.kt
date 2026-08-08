package com.yourcompany.fieldtech.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JobDto(
    @Json(name = "job_id") val jobId: Long,
    @Json(name = "site_id") val siteId: Long,
    val status: String,
    val priority: String? = null,
    @Json(name = "scheduled_at") val scheduledAt: String? = null,
    @Json(name = "site_name") val siteName: String? = null,
    @Json(name = "client_name") val clientName: String? = null
)

@JsonClass(generateAdapter = true)
data class JobDetailDto(
    @Json(name = "job_id") val jobId: Long,
    @Json(name = "site_id") val siteId: Long,
    val status: String,
    val priority: String? = null,
    val assignments: List<Long> = emptyList(),
    val checklist: List<ChecklistItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChecklistItemDto(
    @Json(name = "item_id") val itemId: Long,
    val label: String,
    val completed: Boolean
)

@JsonClass(generateAdapter = true)
data class JobStatusUpdateRequest(
    val status: String
)
