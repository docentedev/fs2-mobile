package com.rojas.disco.data.remote

import com.rojas.disco.BuildConfig
import com.rojas.disco.data.remote.model.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Renueva el accessToken cuando la API responde 401.
 *
 * Usa un cliente Retrofit aparte (sin interceptor ni authenticator) para
 * evitar ciclos. Si la renovación falla, borra los tokens para forzar un
 * nuevo login.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Authenticator {

    private val refreshApi: AuthApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)

    override fun authenticate(route: Route?, response: Response): Request? {
        // No intentar renovar peticiones de autenticación (evita bucles)
        if (response.request.url.encodedPath.contains("/auth/login") ||
            response.request.url.encodedPath.contains("/auth/refresh")
        ) {
            // Borrar tokens si el login/refresh falla con 401
            runBlocking { tokenDataStore.clearTokens() }
            return null
        }

        synchronized(this) {
            val tokenVigente = runBlocking { tokenDataStore.getAccessToken() }
            val tokenUsado = response.request.header("Authorization")?.removePrefix("Bearer ")

            // Otra petición concurrente ya renovó el token: reintentar con el nuevo
            if (tokenVigente != null && tokenVigente != tokenUsado) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $tokenVigente")
                    .build()
            }

            val refreshToken = runBlocking { tokenDataStore.getRefreshToken() }
            if (refreshToken.isNullOrEmpty()) {
                runBlocking { tokenDataStore.clearTokens() }
                return null
            }

            val nuevaRespuesta = try {
                runBlocking { refreshApi.refresh(RefreshTokenRequest(refreshToken)) }
            } catch (e: Exception) {
                runBlocking { tokenDataStore.clearTokens() }
                return null
            }

            runBlocking {
                tokenDataStore.saveTokens(
                    accessToken = nuevaRespuesta.accessToken,
                    refreshToken = nuevaRespuesta.refreshToken ?: refreshToken,
                    idToken = nuevaRespuesta.idToken
                )
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${nuevaRespuesta.accessToken}")
                .build()
        }
    }
}
