package com.rojas.disco.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.rojas.disco.data.local.entities.RolEntity
import com.rojas.disco.data.local.entities.UsuarioEntity
import com.rojas.disco.data.local.entities.UsuarioRolCrossRef

data class RolConUsuarios(
    @Embedded
    val rol: RolEntity,

    @Relation(
        parentColumn = "rolId",
        entityColumn = "usuarioId",
        associateBy = Junction(UsuarioRolCrossRef::class)
    )
    val usuarios: List<UsuarioEntity>
)
