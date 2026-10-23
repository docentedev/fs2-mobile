package com.rojas.disco.ui.components

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

fun generarBitmapQr(contenido: String, ancho: Int = 512, alto: Int = 512): ImageBitmap? {
    return try {
        val matrix = MultiFormatWriter().encode(contenido, BarcodeFormat.QR_CODE, ancho, alto)
        val bmp = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565)
        for (x in 0 until ancho) {
            for (y in 0 until alto) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        bmp.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ImagenCodigoQr(
    contenido: String,
    modifier: Modifier = Modifier,
    tamanoPx: Int = 512
) {
    val qrImageBitmap = remember(contenido) {
        generarBitmapQr(contenido, tamanoPx, tamanoPx)
    }

    if (qrImageBitmap != null) {
        Image(
            bitmap = qrImageBitmap,
            contentDescription = "Codigo QR",
            modifier = modifier
        )
    } else {
        Text("No se pudo generar el codigo QR", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun DialogMostrarQr(
    titulo: String,
    subtitulo: String,
    contenidoQr: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier.padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ImagenCodigoQr(
                            contenido = contenidoQr,
                            modifier = Modifier.size(220.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Codigo de datos: $contenidoQr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun CamaraEscanerQr(
    modifier: Modifier = Modifier,
    onQrDetectado: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermiso = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermiso) {
            lanzadorPermiso.launch(android.Manifest.permission.CAMERA)
        }
    }

    if (!tienePermiso) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Se necesita permiso de camara para escanear.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { lanzadorPermiso.launch(android.Manifest.permission.CAMERA) }) {
                Text("Conceder permiso")
            }
        }
        return
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val onQrDetectadoState = rememberUpdatedState(onQrDetectado)

    DisposableEffect(lifecycleOwner) {
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val scanner = BarcodeScanning.getClient()
                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue?.let { valor ->
                                    onQrDetectadoState.value(valor)
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)

            previewView
        },
        modifier = modifier
    )
}

@Composable
fun DialogEscanearQr(
    titulo: String,
    prompt: String,
    valorSugeridoQr: String = "",
    onQrEscaneado: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputCodigo by remember { mutableStateOf(valorSugeridoQr) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                CamaraEscanerQr(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    onQrDetectado = { codigo ->
                        if (inputCodigo != codigo) {
                            inputCodigo = codigo
                        }
                    }
                )

                Text(
                    text = "Apunta la camara al codigo QR. El codigo detectado se carga automaticamente.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = inputCodigo,
                    onValueChange = { inputCodigo = it },
                    label = { Text("Contenido QR Escaneado") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = inputCodigo.isNotBlank(),
                onClick = {
                    onQrEscaneado(inputCodigo.trim())
                }
            ) {
                Text("Procesar QR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
