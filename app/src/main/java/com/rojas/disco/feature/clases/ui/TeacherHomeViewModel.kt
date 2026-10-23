package com.rojas.disco.feature.clases.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.disco.core.database.local.entities.ClaseEntity
import com.rojas.disco.core.database.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherHomeUiState(
    val profesorId: Long = 0L,
    val clases: List<ClaseEntity> = emptyList()
)

@HiltViewModel
class TeacherHomeViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val profesorId: Long = checkNotNull(
        savedStateHandle.get<Long>("profesorId")
            ?: savedStateHandle.get<String>("profesorId")?.toLongOrNull()
    ) { "profesorId es requerido" }

    val uiState: StateFlow<TeacherHomeUiState> = repository.obtenerClasesPorProfesor(profesorId)
        .map { TeacherHomeUiState(profesorId = profesorId, clases = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TeacherHomeUiState(profesorId = profesorId)
        )

    fun crearClase(nombre: String, descripcion: String, cuestionarioJson: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val nuevaClase = ClaseEntity(
                nombre = nombre,
                descripcion = descripcion,
                profesorId = profesorId,
                cuestionarioEstructuraJson = cuestionarioJson
            )
            repository.crearClase(nuevaClase)
        }
    }
}
