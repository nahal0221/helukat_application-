package com.yourcompany.fieldtech.data.repository

import com.yourcompany.fieldtech.data.local.dao.SyncDao
import com.yourcompany.fieldtech.data.local.entity.JobCacheEntity
import com.yourcompany.fieldtech.data.remote.ApiService
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
        Result.failure(e) // caller falls back to observeJobs() cache
    }

    suspend fun getJobDetail(jobId: Long): Result<JobDetailDto> = try {
        Result.success(api.getJobDetail(jobId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** FT is limited to status-only updates on PATCH /jobs/{id}, per the RBAC notes. */
    suspend fun updateStatus(jobId: Long, status: String): Result<Unit> = try {
        api.updateJobStatus(jobId, com.yourcompany.fieldtech.data.remote.dto.JobStatusUpdateRequest(status))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
