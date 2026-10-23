package com.rojas.disco.core.database.repository

import com.rojas.disco.core.database.local.database.UniversidadDao
import com.rojas.disco.core.database.local.entities.AsistenciaClaseEntity
import com.rojas.disco.core.database.local.entities.ClaseEntity
import com.rojas.disco.core.database.local.entities.RolEntity
import com.rojas.disco.core.database.local.entities.UsuarioEntity
import com.rojas.disco.core.database.local.entities.UsuarioRolCrossRef
import com.rojas.disco.core.database.local.model.ClaseConDetalles
import com.rojas.disco.core.database.local.model.RolConUsuarios
import com.rojas.disco.core.database.local.model.UsuarioConRoles
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UniversidadRepository @Inject constructor(
    private val universidadDao: UniversidadDao
) {
    val todosLosUsuarios: Flow<List<UsuarioEntity>> = universidadDao.getAllUsuarios()
    val todosLosRoles: Flow<List<RolEntity>> = universidadDao.getAllRoles()
    val todasLasClases: Flow<List<ClaseEntity>> = universidadDao.getAllClases()

    // --- GESTIÓN DE USUARIOS Y ROLES ---

    suspend fun crearUsuario(usuario: UsuarioEntity): Long {
        return universidadDao.insertUsuario(usuario)
    }

    suspend fun crearRol(rol: RolEntity): Long {
        return universidadDao.insertRol(rol)
    }

    suspend fun asignarRolAUsuario(usuarioId: Long, rolId: Long) {
        val crossRef = UsuarioRolCrossRef(usuarioId = usuarioId, rolId = rolId)
        universidadDao.insertUsuarioRolCrossRef(crossRef)
    }

    fun obtenerUsuarioConRoles(usuarioId: Long): Flow<UsuarioConRoles?> {
        return universidadDao.getUsuarioConRoles(usuarioId)
    }

    suspend fun buscarUsuarioPorEmail(email: String): UsuarioConRoles? {
        return universidadDao.getUsuarioConRolesByEmail(email)
    }

    fun obtenerRolConUsuarios(rolId: Long): Flow<RolConUsuarios?> {
        return universidadDao.getRolConUsuarios(rolId)
    }

    suspend fun obtenerRolIdPorNombre(nombreRol: String): Long? {
        return universidadDao.getRolIdByNombre(nombreRol)
    }

    suspend fun obtenerTodasLasClases(): List<ClaseEntity> {
        return universidadDao.getAllClasesList()
    }

    // --- GESTIÓN DE CLASES, ASISTENCIA Y CUESTIONARIOS ---

    suspend fun crearClase(clase: ClaseEntity): Long {
        return universidadDao.insertClase(clase)
    }

    suspend fun actualizarClase(clase: ClaseEntity) {
        universidadDao.updateClase(clase)
    }

    fun obtenerClasesPorProfesor(profesorId: Long): Flow<List<ClaseEntity>> {
        return universidadDao.getClasesByProfesor(profesorId)
    }

    fun obtenerClasesPorEstudiante(estudianteId: Long): Flow<List<ClaseEntity>> {
        return universidadDao.getClasesByEstudiante(estudianteId)
    }

    fun obtenerAsistenciaEstudiante(estudianteId: Long, claseId: Long): Flow<AsistenciaClaseEntity?> {
        return universidadDao.getAsistenciaEstudiante(estudianteId, claseId)
    }

    fun obtenerClaseConDetalles(claseId: Long): Flow<ClaseConDetalles?> {
        return universidadDao.getClaseConDetalles(claseId)
    }

    suspend fun obtenerClasePorId(claseId: Long): ClaseEntity? {
        return universidadDao.getClaseById(claseId)
    }

    /**
     * Inscribe a un usuario en una clase como estudiante.
     * REGLA DE NEGOCIO: Un usuario no puede ser estudiante y profesor de la misma clase al mismo tiempo.
     */
    suspend fun inscribirEstudianteEnClase(usuarioId: Long, claseId: Long) {
        val clase = universidadDao.getClaseById(claseId)
            ?: throw IllegalArgumentException("La clase con ID $claseId no existe.")

        if (clase.profesorId == usuarioId) {
            throw IllegalArgumentException("El usuario $usuarioId es el profesor de esta clase y no puede inscribirse como estudiante.")
        }

        val asistenciaInicial = AsistenciaClaseEntity(
            usuarioId = usuarioId,
            claseId = claseId,
            esPresente = false,
            respuestasCuestionarioJson = null
        )
        universidadDao.insertAsistencia(asistenciaInicial)
    }

    /**
     * Registra la asistencia y respuesta de cuestionario.
     * REGLA DE NEGOCIO: Solo si el estudiante está presente puede responder el cuestionario.
     */
    suspend fun registrarAsistenciaYCuestionario(
        usuarioId: Long,
        claseId: Long,
        esPresente: Boolean,
        respuestasJson: String?
    ) {
        val respuestasFinales = if (esPresente) respuestasJson else null

        val asistencia = AsistenciaClaseEntity(
            usuarioId = usuarioId,
            claseId = claseId,
            esPresente = esPresente,
            respuestasCuestionarioJson = respuestasFinales
        )
        universidadDao.insertAsistencia(asistencia)
    }

    fun obtenerEstudiantesPresentes(claseId: Long): Flow<List<AsistenciaClaseEntity>> {
        return universidadDao.getEstudiantesPresentesEnClase(claseId)
    }

    suspend fun eliminarUsuario(usuario: UsuarioEntity) {
        universidadDao.deleteUsuario(usuario)
    }

    suspend fun eliminarClase(clase: ClaseEntity) {
        universidadDao.deleteClase(clase)
    }
}
