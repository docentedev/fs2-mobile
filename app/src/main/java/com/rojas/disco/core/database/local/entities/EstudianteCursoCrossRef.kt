package com.rojas.disco.core.database.local.entities

import androidx.room.Entity

@Entity(
    tableName = "estudiante_curso_cross_ref",
    primaryKeys = ["estudianteId", "cursoId"]
)
data class EstudianteCursoCrossRef(
    val estudianteId: Long,
    val cursoId: Long
)
