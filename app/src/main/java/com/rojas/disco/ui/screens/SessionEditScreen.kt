package com.rojas.disco.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rojas.disco.ui.components.PantallaConBarraSuperior
import com.rojas.disco.ui.viewmodels.SessionEditViewModel

@Composable
fun SessionEditScreen(
    viewModel: SessionEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    PantallaConBarraSuperior(
        titulo = "Editar Clase",
        onNavigateBack = onNavigateBack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                Text("Cargando información para edición...")
            } else if (uiState.claseActual != null) {
                OutlinedTextField(
                    value = uiState.nombre,
                    onValueChange = { viewModel.onNombreChanged(it) },
                    label = { Text("Nombre de la Clase") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.descripcion,
                    onValueChange = { viewModel.onDescripcionChanged(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.cuestionarioJson,
                    onValueChange = { viewModel.onCuestionarioJsonChanged(it) },
                    label = { Text("Estructura Cuestionario (JSON)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.guardarCambios(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }
            } else {
                Text("No se pudo cargar la clase.")
            }
        }
    }
}
