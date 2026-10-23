package com.rojas.disco.feature.clases.ui

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rojas.disco.core.common.qr.CodigoQr
import com.rojas.disco.core.designsystem.components.DialogEscanearQr
import com.rojas.disco.core.designsystem.components.DialogMostrarQr
import com.rojas.disco.core.designsystem.components.PantallaConBarraSuperior
import com.rojas.disco.core.designsystem.components.TarjetaSeccion
import com.rojas.disco.feature.clases.ui.SessionViewModel

@Composable
fun SessionScreen(
    viewModel: SessionViewModel = hiltViewModel(),
    onNavigateToDetail: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialogQrClase by remember { mutableStateOf(false) }
    var showDialogEscanearEstudiante by remember { mutableStateOf(false) }

    val context = LocalContext.current

    PantallaConBarraSuperior(
        titulo = "Resumen de Clase",
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
                TarjetaSeccion(color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = detalles.clase.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detalles.clase.descripcion,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Profesor a Cargo: ${detalles.profesor.nombre}",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Sección de Opciones de Código QR para el Profesor
                TarjetaSeccion(color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        text = "Opciones QR de la Clase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDialogQrClase = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text("Mostrar QR Clase")
                        }

                        Button(
                            onClick = { showDialogEscanearEstudiante = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text("Escanear Estudiante")
                        }
                    }
                }

                TarjetaSeccion {
                    Text(
                        text = "Estructura del Cuestionario (JSON)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = detalles.clase.cuestionarioEstructuraJson,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                TarjetaSeccion {
                    Text(
                        text = "Estudiantes e Inscripciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total inscritos: ${detalles.asistencias.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToDetail,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Ver Detalle")
                    }

                    OutlinedButton(
                        onClick = onNavigateToEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Editar Clase")
                    }
                }
            } ?: run {
                Text("Cargando información de la clase...")
            }
        }

        // Modal para mostrar el QR del Curso (para los estudiantes)
        if (showDialogQrClase) {
            DialogMostrarQr(
                titulo = "Código QR de la Clase",
                subtitulo = "Muestra este código QR a los estudiantes para que accedan directamente a la encuesta de esta clase.",
                contenidoQr = CodigoQr.Clase.generar(uiState.claseId),
                onDismiss = { showDialogQrClase = false }
            )
        }

        // Modal para escanear el QR del Estudiante (para marcar presente)
        if (showDialogEscanearEstudiante) {
            DialogEscanearQr(
                titulo = "Escanear QR de Estudiante",
                prompt = "Apunta la cámara al código QR que muestra el estudiante en su perfil.",
                valorSugeridoQr = CodigoQr.Estudiante.generar(2L),
                onDismiss = { showDialogEscanearEstudiante = false },
                onQrEscaneado = { codigoEscaneado ->
                    val estudianteId = CodigoQr.Estudiante.leer(codigoEscaneado)
                    if (estudianteId != null) {
                        viewModel.marcarEstudiantePresente(estudianteId) {
                            Toast.makeText(
                                context,
                                "¡Estudiante $estudianteId marcado como PRESENTE!",
                                Toast.LENGTH_LONG
                            ).show()
                            showDialogEscanearEstudiante = false
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Código QR inválido. Formato esperado: ${CodigoQr.Estudiante.formato}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }
}
