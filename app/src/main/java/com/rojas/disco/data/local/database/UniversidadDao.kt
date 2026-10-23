package com.rojas.disco.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.rojas.disco.data.local.entities.AsistenciaClaseEntity
import com.rojas.disco.data.local.entities.ClaseEntity
import com.rojas.disco.data.local.entities.RolEntity
import com.rojas.disco.data.local.entities.UsuarioEntity
import com.rojas.disco.data.local.entities.UsuarioRolCrossRef
import com.rojas.disco.data.local.model.ClaseConDetalles
import com.rojas.disco.data.local.model.RolConUsuarios
import com.rojas.disco.data.local.model.UsuarioConRoles
import kotlinx.coroutines.flow.Flow

@Dao
interface UniversidadDao {

    // --- USUARIOS & ROLES ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRol(rol: RolEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuarioRolCrossRef(crossRef: UsuarioRolCrossRef)

    @Query("SELECT * FROM usuarios")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM roles")
    fun getAllRoles(): Flow<List<RolEntity>>

    @Transaction
    @Query("SELECT * FROM usuarios WHERE usuarioId = :usuarioId")
    fun getUsuarioConRoles(usuarioId: Long): Flow<UsuarioConRoles?>

    @Transaction
    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUsuarioConRolesByEmail(email: String): UsuarioConRoles?

    @Transaction
    @Query("SELECT * FROM roles WHERE rolId = :rolId")
    fun getRolConUsuarios(rolId: Long): Flow<RolConUsuarios?>

    @Query("SELECT rolId FROM roles WHERE nombreRol = :nombreRol LIMIT 1")
    suspend fun getRolIdByNombre(nombreRol: String): Long?

    @Query("SELECT * FROM clases")
    suspend fun getAllClasesList(): List<ClaseEntity>


    // --- CLASES & ASISTENCIA ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClase(clase: ClaseEntity): Long

    @Update
    suspend fun updateClase(clase: ClaseEntity)

    @Query("SELECT * FROM clases WHERE claseId = :claseId")
    suspend fun getClaseById(claseId: Long): ClaseEntity?

    @Query("SELECT * FROM clases WHERE profesorId = :profesorId")
    fun getClasesByProfesor(profesorId: Long): Flow<List<ClaseEntity>>

    @Query("SELECT c.* FROM clases c INNER JOIN asistencia_clase a ON c.claseId = a.claseId WHERE a.usuarioId = :estudianteId")
    fun getClasesByEstudiante(estudianteId: Long): Flow<List<ClaseEntity>>

    @Query("SELECT * FROM asistencia_clase WHERE usuarioId = :estudianteId AND claseId = :claseId LIMIT 1")
    fun getAsistenciaEstudiante(estudianteId: Long, claseId: Long): Flow<AsistenciaClaseEntity?>

    @Query("SELECT * FROM clases")
    fun getAllClases(): Flow<List<ClaseEntity>>

    @Transaction
    @Query("SELECT * FROM clases WHERE claseId = :claseId")
    fun getClaseConDetalles(claseId: Long): Flow<ClaseConDetalles?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsistencia(asistencia: AsistenciaClaseEntity)

    @Query("SELECT * FROM asistencia_clase WHERE claseId = :claseId AND esPresente = 1")
    fun getEstudiantesPresentesEnClase(claseId: Long): Flow<List<AsistenciaClaseEntity>>

    @Delete
    suspend fun deleteUsuario(usuario: UsuarioEntity)

    @Delete
    suspend fun deleteClase(clase: ClaseEntity)
}
