package com.rojas.disco.feature.clases.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rojas.disco.core.designsystem.components.EstadoVacio
import com.rojas.disco.core.designsystem.components.TarjetaClase
import com.rojas.disco.feature.clases.ui.TeacherHomeViewModel

@Composable
fun TeacherHomeScreen(
    viewModel: TeacherHomeViewModel = hiltViewModel(),
    onNavigateToSession: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialogCrearClase by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialogCrearClase = true }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Crear nueva clase")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Mis Clases Impartidas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Bienvenido, Profesor. Selecciona una clase para gestionarla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.clases.isEmpty()) {
                EstadoVacio(
                    texto = "No tienes clases creadas aún.",
                    modifier = Modifier.fillMaxSize()
                ) {
                    Button(onClick = { showDialogCrearClase = true }) {
                        Text("Crear Primera Clase")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.clases) { clase ->
                        TarjetaClase(
                            nombre = clase.nombre,
                            descripcion = clase.descripcion,
                            onClick = { onNavigateToSession(clase.claseId) }
                        )
                    }
                }
            }
        }

        if (showDialogCrearClase) {
            DialogCrearClase(
                onDismiss = { showDialogCrearClase = false },
                onClaseCreada = { nombre, descripcion, cuestionarioJson ->
                    viewModel.crearClase(nombre, descripcion, cuestionarioJson)
                    showDialogCrearClase = false
                }
            )
        }
    }
}

@Composable
fun DialogCrearClase(
    onDismiss: () -> Unit,
    onClaseCreada: (String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var cuestionarioJson by remember {
        mutableStateOf("{\"preguntas\":[\"¿Asististe a la sesión?\", \"¿Tienes alguna duda?\"]}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nueva Clase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la Clase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cuestionarioJson,
                    onValueChange = { cuestionarioJson = it },
                    label = { Text("Cuestionario Estructura (JSON)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = nombre.isNotBlank(),
                onClick = {
                    onClaseCreada(nombre, descripcion, cuestionarioJson)
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
