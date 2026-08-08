package com.yourcompany.fieldtech.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_tokens")

@Singleton
class TokenStore @Inject constructor(
    private val context: Context
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    suspend fun save(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
        }
    }

    suspend fun updateAccessToken(accessToken: String) {
        context.dataStore.edit { prefs -> prefs[accessTokenKey] = accessToken }
    }

    suspend fun getAccessToken(): String? = context.dataStore.data.first()[accessTokenKey]

    suspend fun getRefreshToken(): String? = context.dataStore.data.first()[refreshTokenKey]

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
