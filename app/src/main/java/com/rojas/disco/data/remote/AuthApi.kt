package com.rojas.disco.data.remote

import com.rojas.disco.data.remote.model.LoginRequest
import com.rojas.disco.data.remote.model.LoginResponse
import com.rojas.disco.data.remote.model.RefreshTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): LoginResponse
}