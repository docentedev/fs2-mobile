package com.rojas.disco.feature.auth.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.disco.feature.auth.data.AuthRepository
import com.rojas.disco.core.database.repository.UniversidadRepository
import com.rojas.disco.feature.notificaciones.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la sesión al iniciar la app: se usa para el auto-login.
 */
sealed class SesionRestaurada {
    /** Aún se está verificando si hay una sesión guardada. */
    data object Verificando : SesionRestaurada()

    /** No hay tokens válidos (o el usuario ya no existe localmente). */
    data object SinSesion : SesionRestaurada()

    /** Hay una sesión válida; se puede entrar directo al home. */
    data class ConSesion(
        val usuarioId: Long,
        val esEstudiante: Boolean
    ) : SesionRestaurada()
}

@HiltViewModel
class SesionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val universidadRepository: UniversidadRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _sesion = MutableStateFlow<SesionRestaurada>(SesionRestaurada.Verificando)
    val sesion: StateFlow<SesionRestaurada> = _sesion.asStateFlow()

    init {
        viewModelScope.launch {
            _sesion.value = restaurarSesion()
        }
    }

    private suspend fun restaurarSesion(): SesionRestaurada {
        val userInfo = authRepository.getUserInfo()
            ?: run {
                NotificationService.stop(context)
                return SesionRestaurada.SinSesion
            }

        if (authRepository.isTokenExpired()) {
            NotificationService.stop(context)
            return SesionRestaurada.SinSesion
        }

        val usuarioLocal = universidadRepository.buscarUsuarioPorEmail(userInfo.email ?: "")
            ?: run {
                NotificationService.stop(context)
                return SesionRestaurada.SinSesion
            }

        NotificationService.start(context, userInfo.email)

        val esProfesor = userInfo.isProfesor || userInfo.isAdmin
        return SesionRestaurada.ConSesion(
            usuarioId = usuarioLocal.usuario.usuarioId,
            esEstudiante = !esProfesor
        )
    }
}
