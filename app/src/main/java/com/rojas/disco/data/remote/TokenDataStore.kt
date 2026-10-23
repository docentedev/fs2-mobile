package com.rojas.disco.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

// Claves para DataStore
private const val TOKENS_STORE_NAME = "auth_tokens"
private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
private val ID_TOKEN_KEY = stringPreferencesKey("id_token")

private val Context.tokensDataStore: DataStore<Preferences> by preferencesDataStore(
    name = TOKENS_STORE_NAME
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context
    ): TokenDataStore = TokenDataStore(context.tokensDataStore)
}

class TokenDataStore internal constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        idToken: String?
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            if (idToken != null) {
                preferences[ID_TOKEN_KEY] = idToken
            }
        }
    }

    suspend fun getAccessToken(): String? =
        dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()

    suspend fun getRefreshToken(): String? =
        dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()

    suspend fun getIdToken(): String? =
        dataStore.data.map { it[ID_TOKEN_KEY] }.first()

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(ID_TOKEN_KEY)
        }
    }

    // Decodifica el idToken para obtener info del usuario
    suspend fun getUserInfo(): UserInfo? {
        val idToken = getIdToken()
        return if (idToken != null && idToken.isNotEmpty()) {
            try {
                val jwt: DecodedJWT = JWT.decode(idToken)
                val userId = jwt.getClaim("sub").asString()
                val email = jwt.getClaim("email").asString()
                val name = jwt.getClaim("name").asString()
                val phone = jwt.getClaim("phone_number").asString()
                val groups: List<String> = jwt.getClaim("cognito:groups").asList(String::class.java) ?: emptyList()
                UserInfo(userId, email, name, phone, groups)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Verifica si el accessToken está expirado
    suspend fun isAccessTokenExpired(): Boolean {
        val accessToken = getAccessToken()
        return if (accessToken != null && accessToken.isNotEmpty()) {
            try {
                val jwt: DecodedJWT = JWT.decode(accessToken)
                jwt.getExpiresAt().time < System.currentTimeMillis()
            } catch (e: Exception) {
                true
            }
        } else true
    }
}

data class UserInfo(
    val userId: String?,
    val email: String?,
    val name: String?,
    val phone: String?,
    val groups: List<String>
) {
    val isAdmin: Boolean = groups.any { it.equals("administrador", ignoreCase = true) }
    val isProfesor: Boolean = groups.any { it.equals("profesor", ignoreCase = true) }
    val isEstudiante: Boolean = groups.any { it.equals("estudiante", ignoreCase = true) }
}
