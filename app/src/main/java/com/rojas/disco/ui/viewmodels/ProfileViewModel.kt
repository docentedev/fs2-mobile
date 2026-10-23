package com.rojas.disco.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.disco.data.repository.AuthRepository
import com.rojas.disco.data.local.model.UsuarioConRoles
import com.rojas.disco.data.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val usuarioId: Long = 0L,
    val usuarioConRoles: UsuarioConRoles? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val usuarioId: Long = checkNotNull(
        savedStateHandle.get<Long>("usuarioId")
            ?: savedStateHandle.get<String>("usuarioId")?.toLongOrNull()
    ) { "usuarioId es requerido" }

    val uiState: StateFlow<ProfileUiState> = repository.obtenerUsuarioConRoles(usuarioId)
        .map { ProfileUiState(usuarioId = usuarioId, usuarioConRoles = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(usuarioId = usuarioId)
        )

    /** Cierra la sesión: borra los tokens guardados por el login. */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
