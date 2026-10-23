package com.rojas.disco.core.network.remote.model

data class LoginResponse(
    val accessToken: String,
    val expiresIn: Int,
    val idToken: String? = null,
    val refreshToken: String? = null
)
