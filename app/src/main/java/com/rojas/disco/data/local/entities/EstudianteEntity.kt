package com.rojas.disco.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estudiantes")
data class EstudianteEntity(
    @PrimaryKey(autoGenerate = true)
    val estudianteId: Long = 0,
    val nombre: String,
    val email: String
)
