package com.rojas.disco.feature.asistencia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rojas.disco.core.designsystem.components.PantallaConBarraSuperior
import com.rojas.disco.core.designsystem.components.TarjetaSeccion
import com.rojas.disco.feature.asistencia.ui.StudentQuestionnaireViewModel

@Composable
fun StudentQuestionnaireScreen(
    viewModel: StudentQuestionnaireViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    PantallaConBarraSuperior(
        titulo = "Responder Encuesta de Clase",
        onNavigateBack = onNavigateBack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.detalles?.let { detalles ->
                TarjetaSeccion {
                    Text(
                        text = detalles.clase.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = detalles.clase.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Profesor: ${detalles.profesor.nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TarjetaSeccion {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.esPresente,
                            onCheckedChange = { viewModel.onEsPresenteChanged(it) }
                        )
                        Text(
                            text = "Marcar como PRESENTE en esta sesión",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "Estructura Cuestionario (JSON):",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = detalles.clase.cuestionarioEstructuraJson,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.esPresente) {
                        Text(
                            text = "Pregunta 1: ¿Asististe a la clase de hoy?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = uiState.respuesta1,
                            onValueChange = { viewModel.onRespuesta1Changed(it) },
                            label = { Text("Respuesta 1") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Pregunta 2: ¿Qué aprendiste hoy?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = uiState.respuesta2,
                            onValueChange = { viewModel.onRespuesta2Changed(it) },
                            label = { Text("Respuesta 2") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Debes marcarte como PRESENTE para responder el cuestionario de esta clase.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.guardarEncuestaYAsistencia(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Encuesta y Asistencia")
                }
            } ?: run {
                Text("Cargando encuesta de la clase...")
            }
        }
    }
}
