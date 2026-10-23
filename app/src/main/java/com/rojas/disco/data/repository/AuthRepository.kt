package com.rojas.disco.data.repository

import com.rojas.disco.data.remote.AuthApi
import com.rojas.disco.data.remote.LoginResponse
import com.rojas.disco.data.remote.TokenDataStore
import com.rojas.disco.data.remote.model.LoginRequest
import com.rojas.disco.data.remote.UserInfo
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore
) {

    suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            
            // Guardar tokens
            tokenDataStore.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken ?: "",
                idToken = response.idToken
            )

            // Obtener info del usuario del idToken
            val userInfo = tokenDataStore.getUserInfo()
            
            if (userInfo == null) {
                Result.failure(Exception("No se pudo decodificar la información del usuario"))
            } else {
                Result.success(AuthResult(userInfo, response.accessToken))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Boolean {
        return try {
            tokenDataStore.clearTokens()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAccessToken(): String? = tokenDataStore.getAccessToken()

    suspend fun getUserInfo(): UserInfo? = tokenDataStore.getUserInfo()

    suspend fun isTokenExpired(): Boolean = tokenDataStore.isAccessTokenExpired()
}

data class AuthResult(
    val userInfo: UserInfo,
    val accessToken: String
)