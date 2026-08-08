package com.yourcompany.fieldtech.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "expires_in") val expiresIn: Long,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "user_id") val userId: Long,
    val role: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String? = null
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class RefreshResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_in") val expiresIn: Long
)

@JsonClass(generateAdapter = true)
data class RegisterDeviceRequest(
    @Json(name = "device_token") val deviceToken: String,
    val platform: String = "android"
)
