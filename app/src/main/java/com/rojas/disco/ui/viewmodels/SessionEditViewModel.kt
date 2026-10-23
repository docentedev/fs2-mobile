package com.rojas.disco.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.disco.data.local.entities.ClaseEntity
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

data class SessionEditUiState(
    val claseId: Long = 0L,
    val claseActual: ClaseEntity? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val cuestionarioJson: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class SessionEditViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val claseId: Long = checkNotNull(
        savedStateHandle.get<Long>("claseId")
            ?: savedStateHandle.get<String>("claseId")?.toLongOrNull()
    ) { "claseId es requerido" }

    private val _uiState = MutableStateFlow(SessionEditUiState(claseId = claseId))
    val uiState: StateFlow<SessionEditUiState> = _uiState.asStateFlow()

    init {
        cargarClase()
    }

    private fun cargarClase() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val clase = repository.obtenerClasePorId(claseId)
                if (clase != null) {
                    _uiState.update {
                        it.copy(
                            claseActual = clase,
                            nombre = clase.nombre,
                            descripcion = clase.descripcion,
                            cuestionarioJson = clase.cuestionarioEstructuraJson,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onNombreChanged(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun onDescripcionChanged(descripcion: String) {
        _uiState.update { it.copy(descripcion = descripcion) }
    }

    fun onCuestionarioJsonChanged(json: String) {
        _uiState.update { it.copy(cuestionarioJson = json) }
    }

    fun guardarCambios(onSuccess: () -> Unit) {
        val actual = _uiState.value.claseActual ?: return
        val nuevoNombre = _uiState.value.nombre
        val nuevaDescripcion = _uiState.value.descripcion
        val nuevoJson = _uiState.value.cuestionarioJson

        viewModelScope.launch(Dispatchers.IO) {
            val actualizada = actual.copy(
                nombre = nuevoNombre,
                descripcion = nuevaDescripcion,
                cuestionarioEstructuraJson = nuevoJson
            )
            repository.actualizarClase(actualizada)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
