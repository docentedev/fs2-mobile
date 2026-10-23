package com.rojas.disco.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.disco.data.local.model.ClaseConDetalles
import com.rojas.disco.data.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val claseId: Long = 0L,
    val detalles: ClaseConDetalles? = null
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val claseId: Long = checkNotNull(
        savedStateHandle.get<Long>("claseId")
            ?: savedStateHandle.get<String>("claseId")?.toLongOrNull()
    ) { "claseId es requerido" }

    val uiState: StateFlow<SessionUiState> = repository.obtenerClaseConDetalles(claseId)
        .map { SessionUiState(claseId = claseId, detalles = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionUiState(claseId = claseId)
        )

    fun marcarEstudiantePresente(estudianteId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.registrarAsistenciaYCuestionario(
                usuarioId = estudianteId,
                claseId = claseId,
                esPresente = true,
                respuestasJson = null
            )
            launch(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
