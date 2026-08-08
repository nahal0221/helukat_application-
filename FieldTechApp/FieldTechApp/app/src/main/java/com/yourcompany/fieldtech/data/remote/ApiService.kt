package com.yourcompany.fieldtech.data.remote

import com.yourcompany.fieldtech.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Endpoint set used by the Field Technician app, per the API spec's
 * "Client-to-Endpoint Mapping Summary": §4 Jobs (read own), §5 Field Activity,
 * §6 Materials (read + usage logging), §7 Notifications, /sync/batch.
 */
interface ApiService {

    // --- §1 Auth ---
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/register-device")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest): Response<Unit>

    // --- §4 Jobs (role-scoped server-side: FT sees only own) ---
    @GET("jobs")
    suspend fun getJobs(
        @Query("status") status: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 25
    ): List<JobDto>

    @GET("jobs/{id}")
    suspend fun getJobDetail(@Path("id") jobId: Long): JobDetailDto

    // FT is limited to status-only updates on PATCH /jobs/{id}
    @PATCH("jobs/{id}")
    suspend fun updateJobStatus(
        @Path("id") jobId: Long,
        @Body body: JobStatusUpdateRequest
    ): JobDetailDto

    @GET("jobs/{id}/checklist")
    suspend fun getChecklist(@Path("id") jobId: Long): List<ChecklistItemDto>

    @PUT("jobs/{id}/checklist/{itemId}")
    suspend fun completeChecklistItem(
        @Path("id") jobId: Long,
        @Path("itemId") itemId: Long
    ): Response<Unit>

    // --- §5 Field Activity (single-record, online path) ---
    @POST("jobs/{id}/time-logs")
    suspend fun postTimeLog(
        @Path("id") jobId: Long,
        @Body body: TimeLogPayload
    ): Response<Unit>

    @GET("jobs/{id}/time-logs")
    suspend fun getTimeLogs(@Path("id") jobId: Long): List<TimeLogPayload>

    @POST("jobs/{id}/status-updates")
    suspend fun postStatusUpdate(
        @Path("id") jobId: Long,
        @Body body: StatusUpdatePayload
    ): Response<Unit>

    @Multipart
    @POST("jobs/{id}/photos")
    suspend fun uploadPhoto(
        @Path("id") jobId: Long,
        @Part photo: MultipartBody.Part,
        @Part("caption") caption: RequestBody?
    ): Response<Unit>

    @Multipart
    @POST("jobs/{id}/signatures")
    suspend fun submitSignature(
        @Path("id") jobId: Long,
        @Part signature: MultipartBody.Part
    ): Response<Unit>

    // --- §5 Offline batch sync ---
    // Idempotency-Key prevents duplicate records on retried requests (per Design Notes).
    @POST("sync/batch")
    suspend fun syncBatch(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: SyncBatchRequest
    ): SyncBatchResponse

    // --- §6 Materials (read) + usage logging ---
    @GET("materials")
    suspend fun getMaterials(@Query("search") search: String? = null): List<Map<String, Any?>>

    @POST("jobs/{id}/material-usage")
    suspend fun postMaterialUsage(
        @Path("id") jobId: Long,
        @Body body: MaterialUsagePayload
    ): Response<Unit>

    // --- §7 Notifications ---
    @GET("notifications")
    suspend fun getNotifications(@Query("unread") unread: Boolean? = null): List<Map<String, Any?>>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") notificationId: Long): Response<Unit>
}
