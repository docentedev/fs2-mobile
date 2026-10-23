package com.rojas.disco.data.remote

import com.rojas.disco.data.remote.model.LoginRequest
import com.rojas.disco.data.remote.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}