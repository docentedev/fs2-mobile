package com.rojas.disco.notification

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Genera el tópico secreto de ntfy a partir del email del usuario,
 * replicando la lógica del backend (SHA-256, prefijo y longitud).
 */
object NtfyTopics {

    private const val TOPIC_PREFIX = "app_vibe_"
    private const val TOPIC_HASH_LENGTH = 20

    fun generate(email: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(email.trim().lowercase().toByteArray(StandardCharsets.UTF_8))
            val hex = StringBuilder()
            for (b in hash) {
                val h = Integer.toHexString(0xff and b.toInt())
                if (h.length == 1) hex.append('0')
                hex.append(h)
            }
            TOPIC_PREFIX + hex.toString().substring(0, TOPIC_HASH_LENGTH)
        } catch (e: Exception) {
            TOPIC_PREFIX + "default"
        }
    }
}