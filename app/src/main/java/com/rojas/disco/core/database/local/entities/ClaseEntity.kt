package com.rojas.disco.core.database.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clases",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["usuarioId"],
            childColumns = ["profesorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["profesorId"])
    ]
)
data class ClaseEntity(
    @PrimaryKey(autoGenerate = true)
    val claseId: Long = 0,
    val nombre: String,
    val descripcion: String,
    val profesorId: Long, // ID del usuario que imparte la clase como profesor
    val cuestionarioEstructuraJson: String // Estructura/preguntas del cuestionario para esta clase en JSON
)
