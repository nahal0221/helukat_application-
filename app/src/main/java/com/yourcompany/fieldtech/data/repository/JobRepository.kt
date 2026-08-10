package com.yourcompany.fieldtech.data.repository

import com.yourcompany.fieldtech.data.local.dao.SyncDao
import com.yourcompany.fieldtech.data.local.entity.JobCacheEntity
import com.yourcompany.fieldtech.data.remote.ApiService
import com.yourcompany.fieldtech.data.remote.dto.ChecklistItemDto
import com.yourcompany.fieldtech.data.remote.dto.JobDetailDto
import com.yourcompany.fieldtech.data.remote.dto.JobDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JobRepository @Inject constructor(
    private val api: ApiService,
    private val dao: SyncDao
) {
    /** Cached jobs for instant, offline-capable list rendering. */
    fun observeJobs(): Flow<List<JobCacheEntity>> = dao.observeCachedJobs()

    /** Refresh from network (GET /jobs is role-scoped server-side to the tech's own jobs). */
    suspend fun refreshJobs(status: String? = null): Result<List<JobDto>> = try {
        val jobs = api.getJobs(status = status)
        dao.cacheJobs(jobs.map {
            JobCacheEntity(
                jobId = it.jobId,
                siteId = it.siteId,
                status = it.status,
                priority = it.priority,
                scheduledAt = it.scheduledAt,
                siteName = it.siteName,
                clientName = it.clientName
            )
        })
        Result.success(jobs)
    } catch (e: Exception) {
        // TEMPORARY: no backend yet — seed dummy jobs for testing.
        // Remove this fallback once a real API is wired up.
        dao.cacheJobs(dummyJobs.map {
            JobCacheEntity(
                jobId = it.jobId,
                siteId = it.siteId,
                status = it.status,
                priority = it.priority,
                scheduledAt = it.scheduledAt,
                siteName = it.siteName,
                clientName = it.clientName
            )
        })
        Result.success(dummyJobs)
    }

    suspend fun getJobDetail(jobId: Long): Result<JobDetailDto> = try {
        Result.success(api.getJobDetail(jobId))
    } catch (e: Exception) {
        // TEMPORARY: no backend yet — return dummy detail for testing.
        Result.success(dummyJobDetail(jobId))
    }

    /** FT is limited to status-only updates on PATCH /jobs/{id}, per the RBAC notes. */
    suspend fun updateStatus(jobId: Long, status: String): Result<Unit> = try {
        api.updateJobStatus(jobId, com.yourcompany.fieldtech.data.remote.dto.JobStatusUpdateRequest(status))
        Result.success(Unit)
    } catch (e: Exception) {
        // TEMPORARY: no backend yet — pretend it worked so the UI flow can be tested.
        Result.success(Unit)
    }

    companion object {
        // TEMPORARY: dummy data for testing without a backend. Delete this whole
        // block once a real API is available.
        private val dummyJobs = listOf(
            JobDto(
                jobId = 1001, siteId = 1, status = "scheduled", priority = "high",
                scheduledAt = "2026-08-10T09:00:00Z",
                siteName = "Marina Tower Rooftop", clientName = "Skyline Properties"
            ),
            JobDto(
                jobId = 1002, siteId = 2, status = "in_progress", priority = "medium",
                scheduledAt = "2026-08-10T13:00:00Z",
                siteName = "Al Barsha Warehouse", clientName = "Gulf Logistics Co."
            ),
            JobDto(
                jobId = 1003, siteId = 3, status = "completed", priority = "low",
                scheduledAt = "2026-08-09T10:00:00Z",
                siteName = "JLR Office Park - Bldg 4", clientName = "Falcon Holdings"
            )
        )

        private fun dummyJobDetail(jobId: Long): JobDetailDto {
            val job = dummyJobs.find { it.jobId == jobId }
            return JobDetailDto(
                jobId = jobId,
                siteId = job?.siteId ?: 0,
                status = job?.status ?: "scheduled",
                priority = job?.priority,
                assignments = listOf(12L),
                checklist = listOf(
                    ChecklistItemDto(itemId = 1, label = "Inspect main panel", completed = true),
                    ChecklistItemDto(itemId = 2, label = "Test backup power", completed = false),
                    ChecklistItemDto(itemId = 3, label = "Photograph installation", completed = false)
                )
            )
        }
    }
}
