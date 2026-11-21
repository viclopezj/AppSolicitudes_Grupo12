package com.example.appsolicitudes_grupo12.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.appsolicitudes_grupo12.model.data.files.EvidenciaArchivos
import com.example.appsolicitudes_grupo12.model.data.local.AdjuntoEntity
import com.example.appsolicitudes_grupo12.model.solicitud.TiposAdjunto
import com.example.appsolicitudes_grupo12.viewmodel.AdjuntosViewModel

// Imágenes y documentos adicionales de la solicitud. Todos los perfiles que ven la solicitud los ven;
// solo quien puede agregar (puedeAgregar) ve el botón. alAgregar avisa al detalle para refrescarse.
@Composable
fun AdjuntosSolicitud(
    solicitudId: Int,
    usuarioId: Int,
    rol: String,
    puedeAgregar: Boolean,
    alAgregar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdjuntosViewModel = viewModel(
        factory = AdjuntosViewModel.factory(LocalContext.current, solicitudId, usuarioId, rol)
    )
) {
    val adjuntos by viewModel.adjuntos.collectAsState()
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Selector de archivos del sistema. uri es null si se cierra sin elegir.
    val lanzadorArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.onArchivoElegido(uri, alAgregar)
    }

    // Sin adjuntos y sin poder agregar no hay nada que mostrar
    if (adjuntos.isEmpty() && !puedeAgregar) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Documentos y archivos adjuntos (${adjuntos.size})",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (adjuntos.isEmpty()) {
            Text(text = "Sin archivos adjuntos", style = MaterialTheme.typography.bodyLarge)
        }

        adjuntos.forEach { adjunto ->
            FilaAdjunto(
                adjunto = adjunto,
                onAbrir = {
                    if (!abrirArchivo(context, adjunto.uri)) viewModel.avisarNoSePudoAbrir()
                }
            )
        }

        if (estado.cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }

        estado.error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        estado.mensaje?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }

        if (puedeAgregar) {
            OutlinedButton(
                onClick = { lanzadorArchivo.launch(EvidenciaArchivos.TIPOS_ADJUNTO_PERMITIDOS) },
                enabled = !estado.cargando && adjuntos.size < EvidenciaArchivos.MAXIMO_ADJUNTOS,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar documento o imagen")
            }
            Text(
                text = "Hasta ${EvidenciaArchivos.MAXIMO_ADJUNTOS} archivos de 5 MB: imágenes, PDF, TXT o Word.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Un adjunto: miniatura (imágenes) o tipo, nombre, quién y cuándo, y botón para abrirlo.
@Composable
private fun FilaAdjunto(adjunto: AdjuntoEntity, onAbrir: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (adjunto.tipo == TiposAdjunto.IMAGEN) {
            AsyncImage(
                model = adjunto.uri,
                contentDescription = "Imagen adjunta ${adjunto.nombre}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DOC",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = adjunto.nombre,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${adjunto.usuarioNombre} · ${formatearFecha(adjunto.fecha)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onAbrir) { Text("Abrir") }
    }
}

// Abre el archivo con otra app del teléfono. false si no existe o no hay una app que lo abra.
private fun abrirArchivo(context: Context, uriTexto: String): Boolean {
    val uri = EvidenciaArchivos.uriParaAbrir(context, uriTexto) ?: return false
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
