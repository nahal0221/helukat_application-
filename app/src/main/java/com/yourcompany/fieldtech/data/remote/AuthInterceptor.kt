package com.yourcompany.fieldtech.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Attaches the JWT bearer token to every outgoing request except auth endpoints. */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.url.encodedPath.contains("/auth/login") ||
            original.url.encodedPath.contains("/auth/refresh") ||
            original.url.encodedPath.contains("/auth/forgot-password") ||
            original.url.encodedPath.contains("/auth/reset-password")
        ) {
            return chain.proceed(original)
        }

        val token = runBlocking { tokenStore.getAccessToken() }
        val authed = if (token != null) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            original
        }
        return chain.proceed(authed)
    }
}

/**
 * On a 401, attempts one silent refresh via /auth/refresh and retries the original
 * request. If refresh fails, the caller is left unauthenticated and the UI layer
 * should route back to the login screen.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val authApiProvider: () -> ApiService
) : okhttp3.Authenticator {
    override fun authenticate(route: okhttp3.Route?, response: Response): okhttp3.Request? {
        if (responseCount(response) >= 2) return null // avoid infinite retry loops

        val refreshToken = runBlocking { tokenStore.getRefreshToken() } ?: return null
        return try {
            val result = runBlocking {
                authApiProvider().refresh(
                    com.yourcompany.fieldtech.data.remote.dto.RefreshRequest(refreshToken)
                )
            }
            runBlocking { tokenStore.updateAccessToken(result.accessToken) }
            response.request.newBuilder()
                .header("Authorization", "Bearer ${result.accessToken}")
                .build()
        } catch (e: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
