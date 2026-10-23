package com.rojas.disco.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.disco.data.local.entities.ClaseEntity
import com.rojas.disco.data.local.entities.RolEntity
import com.rojas.disco.data.local.entities.UsuarioEntity
import com.rojas.disco.data.repository.AuthRepository
import com.rojas.disco.data.repository.AuthResult
import com.rojas.disco.data.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LoginUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val universidadRepository: UniversidadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail, errorMessage = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(passwordInput = newPassword, errorMessage = null) }
    }

    fun login(
        onSuccessProfesor: (Long) -> Unit,
        onSuccessEstudiante: (Long) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val email = _uiState.value.emailInput.trim()
            val password = _uiState.value.passwordInput

            if (email.isEmpty() || password.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Correo y contraseña son obligatorios."
                    )
                }
                return@launch
            }

            val authResult = withContext(Dispatchers.IO) {
                authRepository.login(email, password)
            }

            authResult.fold(
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error de autenticación: ${exception.message}"
                        )
                    }
                },
                onSuccess = { result ->
                    // Sincronizar usuario con base de datos local
                    val localUserId = withContext(Dispatchers.IO) {
                        syncUserWithLocalDb(result)
                    }

                    if (localUserId != null) {
                        val userInfo = result.userInfo
                        val esProfesor = userInfo.isProfesor || userInfo.isAdmin
                        val esEstudiante = userInfo.isEstudiante || !esProfesor

                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(isLoading = false) }
                            if (esProfesor) {
                                onSuccessProfesor(localUserId)
                            } else if (esEstudiante) {
                                onSuccessEstudiante(localUserId)
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "El usuario no tiene un rol válido (Profesor/Estudiante)."
                                    )
                                }
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error al sincronizar usuario local."
                            )
                        }
                    }
                }
            )
        }
    }

    private suspend fun syncUserWithLocalDb(authResult: AuthResult): Long? {
        val userInfo = authResult.userInfo
        
        // Buscar por email primero
        val existing = universidadRepository.buscarUsuarioPorEmail(userInfo.email ?: "")
        if (existing != null) {
            // Actualizar roles si es necesario
            val rolProfesorId = universidadRepository.obtenerRolIdPorNombre("Profesor")
            val rolEstudianteId = universidadRepository.obtenerRolIdPorNombre("Estudiante")
            
            if (userInfo.isProfesor || userInfo.isAdmin) {
                if (rolProfesorId != null) {
                    universidadRepository.asignarRolAUsuario(existing.usuario.usuarioId, rolProfesorId)
                }
            }
            if (userInfo.isEstudiante) {
                if (rolEstudianteId != null) {
                    universidadRepository.asignarRolAUsuario(existing.usuario.usuarioId, rolEstudianteId)
                }
            }
            return existing.usuario.usuarioId
        }

        // Crear nuevo usuario
        val nombre = userInfo.name ?: userInfo.email?.split("@").first() ?: "Usuario"
        val email = userInfo.email ?: ""
        
        val usuarioId = universidadRepository.crearUsuario(
            UsuarioEntity(nombre = nombre, email = email)
        )

        // Asignar roles
        val rolProfesorId = universidadRepository.obtenerRolIdPorNombre("Profesor")
        val rolEstudianteId = universidadRepository.obtenerRolIdPorNombre("Estudiante")

        if (userInfo.isProfesor || userInfo.isAdmin) {
            if (rolProfesorId != null) {
                universidadRepository.asignarRolAUsuario(usuarioId, rolProfesorId)
            }
        }
        if (userInfo.isEstudiante) {
            if (rolEstudianteId != null) {
                universidadRepository.asignarRolAUsuario(usuarioId, rolEstudianteId)
            }
        }

        // Crear datos demo si es la primera vez (opcional)
        // Se puede quitar si no se desea
        if (universidadRepository.obtenerTodasLasClases().isEmpty()) {
            crearDatosDemoIniciales(usuarioId, email)
        }

        return usuarioId
    }

    private suspend fun crearDatosDemoIniciales(profesorId: Long, email: String) {
        val rolProfesorId = universidadRepository.obtenerRolIdPorNombre("Profesor") ?: 
            universidadRepository.crearRol(RolEntity(nombreRol = "Profesor"))
        val rolEstudianteId = universidadRepository.obtenerRolIdPorNombre("Estudiante") ?: 
            universidadRepository.crearRol(RolEntity(nombreRol = "Estudiante"))

        val claseId = universidadRepository.crearClase(
            ClaseEntity(
                nombre = "Primer Curso",
                descripcion = "Curso de prueba de persistencia local",
                profesorId = profesorId,
                cuestionarioEstructuraJson = "{\"preguntas\":[\"¿Asististe a la clase?\", \"¿Qué aprendiste hoy?\"]}"
            )
        )

        // Crear un estudiante demo si el usuario logueado es profesor
        if (email != "docente.dev@gmail.com") {
            val estudianteId = universidadRepository.crearUsuario(
                UsuarioEntity(nombre = "Estudiante Demo", email = "demo@estudiante.edu")
            )
            universidadRepository.asignarRolAUsuario(estudianteId, rolEstudianteId)
            universidadRepository.inscribirEstudianteEnClase(estudianteId, claseId)
        }
    }
}