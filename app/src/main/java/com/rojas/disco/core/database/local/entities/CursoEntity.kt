package com.rojas.disco.core.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cursos")
data class CursoEntity(
    @PrimaryKey(autoGenerate = true)
    val cursoId: Long = 0,
    val nombre: String,
    val codigo: String
)
