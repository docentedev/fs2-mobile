package com.rojas.disco.core.database.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "asistencia_clase",
    primaryKeys = ["usuarioId", "claseId"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["usuarioId"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClaseEntity::class,
            parentColumns = ["claseId"],
            childColumns = ["claseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["claseId"])
    ]
)
data class AsistenciaClaseEntity(
    val usuarioId: Long, // ID del usuario inscrito como estudiante
    val claseId: Long,
    val esPresente: Boolean = false,
    val respuestasCuestionarioJson: String? = null // Respuestas en JSON (solo si esPresente = true)
)
