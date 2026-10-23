package com.rojas.disco.core.network.remote.model

/**
 * Envolvente estándar de las respuestas del backend (`ApiResponse<T>`).
 */
data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null
)