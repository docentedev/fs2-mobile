package com.rojas.disco.core.database.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.rojas.disco.core.database.local.entities.AsistenciaClaseEntity
import com.rojas.disco.core.database.local.entities.ClaseEntity
import com.rojas.disco.core.database.local.entities.UsuarioEntity

data class ClaseConDetalles(
    @Embedded
    val clase: ClaseEntity,

    @Relation(
        parentColumn = "profesorId",
        entityColumn = "usuarioId"
    )
    val profesor: UsuarioEntity,

    @Relation(
        parentColumn = "claseId",
        entityColumn = "claseId"
    )
    val asistencias: List<AsistenciaClaseEntity>
)
