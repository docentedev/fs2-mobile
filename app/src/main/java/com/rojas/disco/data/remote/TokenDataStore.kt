package com.rojas.disco.data.remote

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rxjava3.RxDataStore
import androidx.datastore.preferences.rxjava3.RxDataStoreBuilder
import androidx.datastore.preferences.rxjava3.toMutablePreferences
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.core.Single
import javax.inject.Singleton
import kotlinx.coroutines.await

// Claves para DataStore
private const val TOKENS_STORE_NAME = "auth_tokens"
private val ACCESS_TOKEN_KEY = preferencesKey<String>("access_token")
private val REFRESH_TOKEN_KEY = preferencesKey<String>("refresh_token")
private val ID_TOKEN_KEY = preferencesKey<String>("id_token")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTokenDataStore(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): TokenDataStore {
        val dataStore: RxDataStore<Preferences> = RxDataStoreBuilder(context, TOKENS_STORE_NAME).build()
        return TokenDataStore(dataStore)
    }
}

class TokenDataStore private constructor(
    private val dataStore: RxDataStore<Preferences>
) {

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        idToken: String?
    ) {
        dataStore.updateDataAsync { preferences ->
            preferences.toMutablePreferences().apply {
                put(ACCESS_TOKEN_KEY, accessToken)
                put(REFRESH_TOKEN_KEY, refreshToken)
                idToken?.let { put(ID_TOKEN_KEY, it) }
            }
        }.await()
    }

    suspend fun getAccessToken(): String? = dataStore.data
        .map { it[ACCESS_TOKEN_KEY] }
        .firstOrNull()

    suspend fun getRefreshToken(): String? = dataStore.data
        .map { it[REFRESH_TOKEN_KEY] }
        .firstOrNull()

    suspend fun getIdToken(): String? = dataStore.data
        .map { it[ID_TOKEN_KEY] }
        .firstOrNull()

    suspend fun clearTokens() {
        dataStore.updateDataAsync { preferences ->
            preferences.toMutablePreferences().apply {
                remove(ACCESS_TOKEN_KEY)
                remove(REFRESH_TOKEN_KEY)
                remove(ID_TOKEN_KEY)
            }
        }.await()
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