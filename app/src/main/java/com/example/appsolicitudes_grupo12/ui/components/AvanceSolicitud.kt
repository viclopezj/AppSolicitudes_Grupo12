package com.example.appsolicitudes_grupo12.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaArchivos
import com.example.appsolicitudes_grupo12.ui.camera.CameraPermissionHelper
import com.example.appsolicitudes_grupo12.viewmodel.AvanceSolicitudViewModel
import java.io.File

// Formulario del técnico para registrar un avance: texto, foto opcional y opción de comentario interno.
// onEnviado se llama al guardar, para que el detalle se actualice.
@Composable
fun AvanceSolicitud(
    id: Int,
    usuarioId: Int,
    rol: String,
    onEnviado: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AvanceSolicitudViewModel = viewModel(
        factory = AvanceSolicitudViewModel.factory(LocalContext.current, id, usuarioId, rol)
    )
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    val ocupado = estado.cargandoFoto || estado.enviando

    // Ruta donde la cámara escribe la foto (se guarda por si la app se reinicia con la cámara abierta)
    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    // ---------- Lanzadores de cámara, permiso y galería ----------

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaFotoPendiente
        rutaFotoPendiente = null
        if (ruta != null) {
            val archivo = File(ruta)
            if (exito) viewModel.onFotoTomada(archivo) else viewModel.onFotoCancelada(archivo)
        }
    }

    val abrirCamara = {
        val archivo = viewModel.crearArchivoParaCamara()
        rutaFotoPendiente = archivo.absolutePath
        lanzadorCamara.launch(EvidenciaArchivos.uriParaCamara(context, archivo))
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else viewModel.onPermisoCamaraDenegado()
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onFotoElegida(uri)
    }

    // ---------- Pantalla ----------

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Registrar avance", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = estado.texto,
            onValueChange = viewModel::onTextoChange,
            label = { Text("Avance o comentario") },
            minLines = 3,
            maxLines = 6,
            enabled = !estado.enviando,
            isError = estado.errorTexto != null,
            supportingText = { estado.errorTexto?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = estado.esInterno,
                onCheckedChange = viewModel::onInternoChange,
                enabled = !estado.enviando
            )
            Text(
                text = "Comentario interno (el cliente no lo ve)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }

        // Vista previa: indicador mientras se copia la foto, y la foto cuando ya hay una
        if (estado.cargandoFoto) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (estado.evidenciaUri != null) {
            AsyncImage(
                model = estado.evidenciaUri,
                contentDescription = "Foto de evidencia del avance",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    // Con permiso abre la cámara; sin permiso lo pide primero
                    if (CameraPermissionHelper.hasCameraPermission(context)) {
                        abrirCamara()
                    } else {
                        lanzadorPermiso.launch(Manifest.permission.CAMERA)
                    }
                },
                enabled = !ocupado,
                modifier = Modifier.weight(1f)
            ) {
                Text("Tomar foto")
            }

            OutlinedButton(
                onClick = {
                    lanzadorGaleria.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !ocupado,
                modifier = Modifier.weight(1f)
            ) {
                Text("Elegir de galería")
            }
        }

        if (estado.evidenciaUri != null) {
            TextButton(onClick = viewModel::quitarFoto, enabled = !ocupado) {
                Text("Quitar foto")
            }
        }

        estado.error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        estado.mensaje?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = { viewModel.enviar(onEnviado) },
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar avance")
        }

        // Regla del caso: solo datos ficticios
        Text(
            text = "Usa solo imágenes de prueba, sin datos personales.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}