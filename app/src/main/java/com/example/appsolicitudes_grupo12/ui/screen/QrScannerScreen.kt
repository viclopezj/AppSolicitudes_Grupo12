package com.example.appsolicitudes_grupo12.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsolicitudes_grupo12.model.qr.DatosQr
import com.example.appsolicitudes_grupo12.state.QrUiState
import com.example.appsolicitudes_grupo12.ui.adaptive.AppWindowSizeClass
import com.example.appsolicitudes_grupo12.ui.adaptive.adaptiveContentWidth
import com.example.appsolicitudes_grupo12.ui.camera.CameraPermissionHelper
import com.example.appsolicitudes_grupo12.ui.camera.QrScanner
import com.example.appsolicitudes_grupo12.ui.components.EtiquetaEstado
import com.example.appsolicitudes_grupo12.viewmodel.QrViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Pantalla del escáner QR: pide el permiso de cámara, muestra la cámara y, al leer un QR
// de solicitud, muestra los datos que traía. Solo muestra el estado del ViewModel y le envía eventos.
// onVolver: flecha de la barra superior. onAbrirDetalle: recibe el id de la solicitud a abrir.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    usuarioId: Int,
    rol: String,
    viewModel: QrViewModel = viewModel(
        factory = QrViewModel.factory(LocalContext.current, usuarioId, rol)
    ),
    windowSizeClass: AppWindowSizeClass = AppWindowSizeClass.Compact,
    onVolver: () -> Unit = {},
    onAbrirDetalle: (Int) -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Si la app ya tiene permiso de cámara (parte con lo que diga el sistema)
    var tienePermiso by remember {
        mutableStateOf(CameraPermissionHelper.hasCameraPermission(context))
    }

    // Abre el cuadro "¿Permitir usar la cámara?" y guarda la respuesta
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermiso = concedido
    }

    // Al abrir la pantalla, si no hay permiso, se pide automáticamente
    LaunchedEffect(Unit) {
        if (!tienePermiso) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Escanear solicitud") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingInterno ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno),
            contentAlignment = Alignment.TopCenter
        ) {
            // Centra y limita el área de cámara en ventanas grandes
            Box(
                modifier = Modifier
                    .adaptiveContentWidth(windowSizeClass, 600.dp, 760.dp, 1000.dp)
                    .fillMaxSize()
            ) {
                if (!tienePermiso) {
                    ContenidoSinPermiso(
                        onPedirPermiso = { lanzadorPermiso.launch(Manifest.permission.CAMERA) }
                    )
                } else {
                    // Se dibuja según el estado del ViewModel
                    when (val actual = estado) {
                        is QrUiState.Escaneando -> ContenidoEscaneando(
                            onQrDetectado = viewModel::onQrDetectado
                        )
                        is QrUiState.Buscando -> ContenidoBuscando()
                        is QrUiState.Leida -> ContenidoLeida(
                            datos = actual.datos,
                            idLocal = actual.idLocal,
                            onAbrirDetalle = onAbrirDetalle,
                            onEscanearOtro = viewModel::reiniciar
                        )
                        is QrUiState.NoValido -> ContenidoNoValido(
                            onEscanearOtro = viewModel::reiniciar
                        )
                    }
                }
            }
        }
    }
}

// Sin permiso: explica por qué se necesita y permite volver a pedirlo.
@Composable
private fun ContenidoSinPermiso(onPedirPermiso: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Se necesita permiso de cámara",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Para leer el código QR de una solicitud, la app necesita usar la cámara. " +
                    "Si el cuadro de permiso ya no aparece, actívalo en Ajustes > Aplicaciones.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(
            onClick = onPedirPermiso,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Conceder permiso de cámara")
        }
    }
}

// Escaneando: la cámara con un marco para apuntar al QR.
@Composable
private fun ContenidoEscaneando(onQrDetectado: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            QrScanner(
                onQrCodeScanned = onQrDetectado,
                modifier = Modifier.fillMaxSize()
            )

            // Marco solo como guía visual: el QR se lee en cualquier parte de la imagen
            Surface(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.Center),
                color = Color.Transparent,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {}
        }

        Text(
            text = "Enfoca el código QR de una solicitud",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

// Buscando: se revisa si la solicitud existe en este teléfono.
@Composable
private fun ContenidoBuscando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// Leída: los datos que venían dentro del QR.
@Composable
private fun ContenidoLeida(
    datos: DatosQr,
    idLocal: Int?,
    onAbrirDetalle: (Int) -> Unit,
    onEscanearOtro: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Código QR leído",
            style = MaterialTheme.typography.titleMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Código a la izquierda, estado a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = datos.codigo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    EtiquetaEstado(datos.estado)
                }

                Text(
                    text = datos.titulo,
                    style = MaterialTheme.typography.headlineSmall
                )

                CampoQr(etiqueta = "Descripción", valor = datos.descripcion)
                CampoQr(etiqueta = "Categoría", valor = datos.categoria)
                CampoQr(
                    etiqueta = "Servicio relacionado",
                    valor = datos.servicio.ifBlank { "No indicado" }
                )
                CampoQr(
                    etiqueta = "Fecha de creación",
                    valor = fechaLegible(datos.fechaCreacion)
                )
            }
        }

        if (idLocal != null) {
            // La solicitud también está en este teléfono: se puede abrir su detalle
            Button(
                onClick = { onAbrirDetalle(idLocal) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir detalle")
            }
        } else {
            // El QR viene de otro teléfono: solo se conocen los datos que traía
            Text(
                text = "Esta solicitud no está guardada en este teléfono. " +
                        "Se muestran los datos que traía el código QR.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedButton(
            onClick = onEscanearOtro,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Escanear otro código")
        }
    }
}

// No válido: el QR no es de una solicitud de esta app.
@Composable
private fun ContenidoNoValido(onEscanearOtro: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Este código QR no es de una solicitud",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Prueba con el QR que aparece al registrar una solicitud o en su detalle.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(
            onClick = onEscanearOtro,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Escanear otro código")
        }
    }
}

// ---------- Piezas de apoyo ----------

// Un dato de solo lectura: etiqueta pequeña arriba y el valor debajo.
@Composable
private fun CampoQr(etiqueta: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Convierte los milisegundos a texto legible, por ejemplo "17/10/2026 14:35".
private fun fechaLegible(milisegundos: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formato.format(Date(milisegundos))
}