package com.rojas.disco.core.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val usuarioId: Long = 0,
    val nombre: String,
    val email: String
)
