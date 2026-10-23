package com.rojas.disco.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "usuario_rol_cross_ref",
    primaryKeys = ["usuarioId", "rolId"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["usuarioId"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["rolId"],
            childColumns = ["rolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["rolId"])
    ]
)
data class UsuarioRolCrossRef(
    val usuarioId: Long,
    val rolId: Long
)
