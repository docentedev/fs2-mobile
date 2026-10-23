package com.rojas.disco.core.common.qr

/**
 * Formatos de codigos QR que usa la aplicacion.
 *
 * Centraliza los prefijos y la conversion id <-> contenido del QR para no
 * repetir "CLASE_"/"ESTUDIANTE_" en cada pantalla.
 */
sealed class CodigoQr(val prefijo: String) {

    /** Ejemplo legible del formato, util para mensajes de error. */
    val formato: String
        get() = "$prefijo{id}"

    /** Genera el contenido que se dibuja en el codigo QR. */
    fun generar(id: Long): String = "$prefijo$id"

    /** Extrae el id de un contenido escaneado, o null si no corresponde al formato. */
    fun leer(codigo: String): Long? =
        codigo.trim()
            .takeIf { it.startsWith(prefijo) }
            ?.removePrefix(prefijo)
            ?.toLongOrNull()

    data object Clase : CodigoQr("CLASE_")

    data object Estudiante : CodigoQr("ESTUDIANTE_")
}
