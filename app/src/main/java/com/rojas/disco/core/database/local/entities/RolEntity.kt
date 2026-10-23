package com.rojas.disco.core.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class RolEntity(
    @PrimaryKey(autoGenerate = true)
    val rolId: Long = 0,
    val nombreRol: String // "Estudiante", "Coordinador", "Profesor", "Administrador"
)
