package com.yourcompany.fieldtech.data.repository

import com.yourcompany.fieldtech.data.remote.ApiService
import com.yourcompany.fieldtech.data.remote.TokenStore
import com.yourcompany.fieldtech.data.remote.dto.LoginRequest
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {
suspend fun login(email: String, password: String): Result<Unit> {
    // TEMPORARY: dummy login for testing without a real backend.
    // Remove this block once a real API is wired up.
    if (email == "test@test.com" && password == "test123") {
        tokenStore.save("dummy_access_token", "dummy_refresh_token")
        return Result.success(Unit)
    }

    return try {
        val response = api.login(LoginRequest(email, password))
        tokenStore.save(response.accessToken, response.refreshToken)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    suspend fun logout() {
        runCatching { api.logout() }
        tokenStore.clear()
    }

    suspend fun isLoggedIn(): Boolean = tokenStore.getAccessToken() != null
}
