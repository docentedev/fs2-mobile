package com.rojas.disco.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Estado vacio reutilizable para listas sin datos.
 *
 * [accion] permite agregar un boton debajo del mensaje.
 */
@Composable
fun EstadoVacio(
    texto: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (accion != null) {
            Spacer(modifier = Modifier.height(12.dp))
            accion()
        }
    }
}
