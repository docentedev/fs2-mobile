package com.rojas.disco.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.rojas.disco.data.local.entities.RolEntity
import com.rojas.disco.data.local.entities.UsuarioEntity
import com.rojas.disco.data.local.entities.UsuarioRolCrossRef

data class UsuarioConRoles(
    @Embedded
    val usuario: UsuarioEntity,

    @Relation(
        parentColumn = "usuarioId",
        entityColumn = "rolId",
        associateBy = Junction(UsuarioRolCrossRef::class)
    )
    val roles: List<RolEntity>
)
