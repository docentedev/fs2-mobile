package com.rojas.disco.feature.clases.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rojas.disco.core.designsystem.components.PantallaConBarraSuperior
import com.rojas.disco.core.designsystem.components.TarjetaSeccion
import com.rojas.disco.feature.clases.ui.SessionDetailViewModel

@Composable
fun SessionDetailScreen(
    viewModel: SessionDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    PantallaConBarraSuperior(
        titulo = "Detalle de la Sesión",
        onNavigateBack = onNavigateBack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            uiState.detalles?.let { detalles ->
                Text(
                    text = detalles.clase.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Respuestas y Asistencias de la Clase",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (detalles.asistencias.isEmpty()) {
                    Text("No hay registros de asistencia para esta clase.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(detalles.asistencias) { asistencia ->
                            val estudianteConRoles by viewModel.obtenerUsuarioConRoles(asistencia.usuarioId)
                                .collectAsState(initial = null)

                            TarjetaSeccion(
                                color = if (asistencia.esPresente) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                            ) {
                                Text(
                                    text = "Estudiante: ${estudianteConRoles?.usuario?.nombre ?: "ID ${asistencia.usuarioId}"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Email: ${estudianteConRoles?.usuario?.email ?: ""}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    text = "Asistencia: ${if (asistencia.esPresente) "PRESENTE" else "AUSENTE"}",
                                    fontWeight = FontWeight.Bold
                                )

                                asistencia.respuestasCuestionarioJson?.let { respuestas ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Respuestas Cuestionario (JSON):",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = respuestas,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Text("Cargando detalle de sesión...")
            }
        }
    }
}
