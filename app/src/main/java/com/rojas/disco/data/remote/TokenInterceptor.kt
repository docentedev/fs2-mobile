package com.rojas.disco.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor @javax.inject.Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // No agregar token a la petición de login
        if (originalRequest.url.encodedPath.contains("/auth/login")) {
            return chain.proceed(originalRequest)
        }

        val accessToken = withContext(Dispatchers.IO) {
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