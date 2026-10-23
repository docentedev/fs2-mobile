package com.rojas.disco.data.repository

import com.rojas.disco.data.remote.AuthApi
import com.rojas.disco.data.remote.TokenDataStore
import com.rojas.disco.data.remote.model.LoginRequest
import com.rojas.disco.data.remote.model.LoginResponse
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
            val apiResponse = authApi.login(LoginRequest(email, password))

            if (!apiResponse.success || apiResponse.data == null) {
                return Result.failure(Exception(apiResponse.message ?: "Error de autenticación"))
            }

            val response = apiResponse.data

            // Guardar tokens
            tokenDataStore.saveTokens(
                accessToken = response.accessToken ?: "",
                refreshToken = response.refreshToken ?: "",
                idToken = response.idToken
            )

            // Obtener info del usuario del idToken
            val userInfo = tokenDataStore.getUserInfo()

            val accessToken = response.accessToken
            if (userInfo == null || accessToken.isNullOrBlank()) {
                Result.failure(Exception("No se pudo decodificar la información del usuario"))
            } else {
                Result.success(AuthResult(userInfo, accessToken))
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