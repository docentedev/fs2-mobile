package com.rojas.disco.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Tarjeta con el padding y los colores que usan las secciones de la app.
 */
@Composable
fun TarjetaSeccion(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = contenido
        )
    }
}

/**
 * Tarjeta reutilizable para listar una clase.
 *
 * [encabezado] permite agregar informacion al lado del nombre (por ejemplo el
 * estado de asistencia) y [pie] contenido adicional debajo de la descripcion.
 */
@Composable
fun TarjetaClase(
    nombre: String,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorContenedor: Color = MaterialTheme.colorScheme.surfaceVariant,
    encabezado: (@Composable RowScope.() -> Unit)? = null,
    pie: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorContenedor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                encabezado?.invoke(this)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            if (pie != null) {
                Spacer(modifier = Modifier.height(8.dp))
                pie()
            }
        }
    }
}
