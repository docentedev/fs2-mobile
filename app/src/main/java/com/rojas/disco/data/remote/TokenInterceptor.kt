package com.rojas.disco.data.remote

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor @javax.inject.Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // No agregar token a las peticiones de autenticación
        val path = originalRequest.url.encodedPath
        if (path.contains("/auth/login") || path.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        // intercept() no es suspend; se lee el token de forma bloqueante
        val accessToken = kotlinx.coroutines.runBlocking {
            tokenDataStore.getAccessToken()
        }

        return if (accessToken != null && accessToken.isNotEmpty()) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}