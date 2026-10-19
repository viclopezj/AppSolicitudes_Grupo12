package com.example.appsolicitudes_grupo12.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appsolicitudes_grupo12.utils.QrScanner
import com.example.appsolicitudes_grupo12.viewModel.QrViewModel

@Composable
fun QrScannerScreen(
    viewModel: QrViewModel,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val qrResult by viewModel.qrResult.observeAsState()
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasCameraPermission) {
            Text(
                "Permiso de cámara requerido",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Para escanear códigos QR, necesitamos acceso a la cámara",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission
            ) {
                Text("Conceder permiso de cámara")
            }
        } else if (qrResult == null && isScanning) {
            Text(
                "Escanea un código QR",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Usar el nuevo scanner con CameraX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                QrScanner(
                    onQrCodeScanned = { qrContent ->
                        // Procesar el QR detectado
                        viewModel.onQrDetected(qrContent)
                        isScanning = false
                        Toast.makeText(context, "QR detectado!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay para ayudar al escaneo
                Surface(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.Center),
                    color = Color.Transparent,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    )
                ) {}
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Enfoca el código QR en el marco central",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "La cámara debería activarse automáticamente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (qrResult != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    " QR Detectado:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        qrResult!!.content,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.clearResult()
                        isScanning = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Escanear otro código QR")
                }
            }
        }
    }
}