package com.rojas.disco.feature.asistencia.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rojas.disco.core.database.local.entities.AsistenciaClaseEntity
import com.rojas.disco.core.database.local.model.ClaseConDetalles
import com.rojas.disco.core.database.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class StudentQuestionnaireUiState(
    val claseId: Long = 0L,
    val estudianteId: Long = 0L,
    val detalles: ClaseConDetalles? = null,
    val asistenciaActual: AsistenciaClaseEntity? = null,
    val esPresente: Boolean = true,
    val respuesta1: String = "Sí, asistí a la sesión de hoy",
    val respuesta2: String = "Aprendí el funcionamiento de las entidades de Room"
)

@HiltViewModel
class StudentQuestionnaireViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val claseId: Long = checkNotNull(
        savedStateHandle.get<Long>("claseId")
            ?: savedStateHandle.get<String>("claseId")?.toLongOrNull()
    ) { "claseId es requerido" }

    val estudianteId: Long = checkNotNull(
        savedStateHandle.get<Long>("estudianteId")
            ?: savedStateHandle.get<String>("estudianteId")?.toLongOrNull()
    ) { "estudianteId es requerido" }

    private val _esPresenteState = MutableStateFlow(true)
    private val _respuesta1State = MutableStateFlow("Sí, asistí a la sesión de hoy")
    private val _respuesta2State = MutableStateFlow("Aprendí el funcionamiento de las entidades de Room")

    val uiState: StateFlow<StudentQuestionnaireUiState> = combine(
        repository.obtenerClaseConDetalles(claseId),
        repository.obtenerAsistenciaEstudiante(estudianteId, claseId),
        _esPresenteState,
        _respuesta1State,
        _respuesta2State
    ) { detalles, asistencia, esPresente, r1, r2 ->
        val esPresenteEfectivo = asistencia?.esPresente ?: esPresente
        StudentQuestionnaireUiState(
            claseId = claseId,
            estudianteId = estudianteId,
            detalles = detalles,
            asistenciaActual = asistencia,
            esPresente = esPresenteEfectivo,
            respuesta1 = r1,
            respuesta2 = r2
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudentQuestionnaireUiState(
            claseId = claseId,
            estudianteId = estudianteId
        )
    )

    fun onEsPresenteChanged(esPresente: Boolean) {
        _esPresenteState.update { esPresente }
    }

    fun onRespuesta1Changed(r1: String) {
        _respuesta1State.update { r1 }
    }

    fun onRespuesta2Changed(r2: String) {
        _respuesta2State.update { r2 }
    }

    fun guardarEncuestaYAsistencia(onSuccess: () -> Unit) {
        val currentState = uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            val jsonRespuestas = if (currentState.esPresente) {
                "{\"respuestas\":[\"${currentState.respuesta1}\", \"${currentState.respuesta2}\"]}"
            } else null

            repository.registrarAsistenciaYCuestionario(
                usuarioId = estudianteId,
                claseId = claseId,
                esPresente = currentState.esPresente,
                respuestasJson = jsonRespuestas
            )

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
