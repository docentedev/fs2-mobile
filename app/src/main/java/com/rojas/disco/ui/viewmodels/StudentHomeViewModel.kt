package com.rojas.disco.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.disco.data.local.entities.ClaseEntity
import com.rojas.disco.data.local.model.UsuarioConRoles
import com.rojas.disco.data.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StudentHomeUiState(
    val estudianteId: Long = 0L,
    val usuarioInfo: UsuarioConRoles? = null,
    val clases: List<ClaseEntity> = emptyList()
)

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val estudianteId: Long = checkNotNull(
        savedStateHandle.get<Long>("estudianteId")
            ?: savedStateHandle.get<String>("estudianteId")?.toLongOrNull()
    ) { "estudianteId es requerido" }

    val uiState: StateFlow<StudentHomeUiState> = combine(
        repository.obtenerUsuarioConRoles(estudianteId),
        repository.obtenerClasesPorEstudiante(estudianteId)
    ) { usuario, clases ->
        StudentHomeUiState(
            estudianteId = estudianteId,
            usuarioInfo = usuario,
            clases = clases
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudentHomeUiState(estudianteId = estudianteId)
    )

    fun obtenerAsistenciaEstudiante(claseId: Long) = repository.obtenerAsistenciaEstudiante(estudianteId, claseId)
}
