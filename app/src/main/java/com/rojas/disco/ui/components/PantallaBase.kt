package com.rojas.disco.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Scaffold comun para pantallas secundarias: barra superior con titulo y boton
 * de volver. Evita repetir el mismo TopAppBar en cada pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConBarraSuperior(
    titulo: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    acciones: @Composable RowScope.() -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = titulo) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = acciones
            )
        },
        content = contenido
    )
}
